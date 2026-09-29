import { useEffect, useState } from 'react'
import { inscripcionesApi, actividadesApi, clientesApi, formatoFechaHora } from '../../api/client.js'
import { Card, PageHeader, Spinner, Alert, Table, EmptyState, Button, Modal, Input, Select, ConfirmDialog } from '../../components/ui.jsx'

export default function InscripcionesAdmin() {
  const [actividades, setActividades] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(null)
  const [inscripciones, setInscripciones] = useState([])
  const [modo, setModo] = useState('actividad')
  const [actividadSel, setActividadSel] = useState('')
  const [dniCliente, setDniCliente] = useState('')
  const [clienteInfo, setClienteInfo] = useState(null)
  const [mostrarForm, setMostrarForm] = useState(false)
  const [anular, setAnular] = useState(null)

  useEffect(() => {
    actividadesApi
      .listar()
      .then(setActividades)
      .catch((err) => setError(err.message))
      .finally(() => setCargando(false))
  }, [])

  const cargarInscripciones = async () => {
    setError(null)
    setInscripciones([])
    if (modo === 'actividad') {
      if (!actividadSel) return
      try {
        setInscripciones(await inscripcionesApi.listarPorActividad(Number(actividadSel)))
      } catch (err) {
        setError(err.message)
      }
    } else {
      if (!clienteInfo) return
      try {
        setInscripciones(await inscripcionesApi.listarPorCliente(clienteInfo.id))
      } catch (err) {
        setError(err.message)
      }
    }
  }

  const resolverCliente = async () => {
    if (!dniCliente.trim()) return
    try {
      const cliente = await clientesApi.obtenerPorDni(dniCliente.trim())
      setClienteInfo(cliente)
      setError(null)
    } catch (err) {
      setClienteInfo(null)
      setError(`Cliente no encontrado: ${err.message}`)
    }
  }

  const confirmarAnular = async () => {
    try {
      await inscripcionesApi.anular(anular.id)
      setAnular(null)
      cargarInscripciones()
    } catch (err) {
      setError(err.message)
      setAnular(null)
    }
  }

  if (cargando) return <Spinner />

  return (
    <div className="space-y-6">
      <PageHeader title="Gestión de inscripciones" subtitle="Consulta y administra inscripciones a actividades">
        <Button onClick={() => setMostrarForm(true)}>+ Nueva inscripción</Button>
      </PageHeader>

      {error && (
        <div className="mb-4">
          <Alert>{error}</Alert>
        </div>
      )}

      <Card className="p-5">
        <div className="flex flex-wrap items-end gap-3">
          <Select
            label="Filtrar por"
            value={modo}
            onChange={(e) => { setModo(e.target.value); setInscripciones([]); setClienteInfo(null) }}
            className="w-44"
          >
            <option value="actividad">Actividad</option>
            <option value="cliente">Cliente</option>
          </Select>

          {modo === 'actividad' ? (
            <div className="min-w-64 flex-1">
              <Select
                label="Actividad"
                value={actividadSel}
                onChange={(e) => setActividadSel(e.target.value)}
              >
                <option value="">Selecciona una actividad…</option>
                {actividades.map((a) => (
                  <option key={a.id} value={a.id}>
                    {a.nombre} ({a.estado})
                  </option>
                ))}
              </Select>
            </div>
          ) : (
            <div className="min-w-64 flex-1">
              <Input
                label="DNI del cliente"
                placeholder="12345678A"
                value={dniCliente}
                onChange={(e) => { setDniCliente(e.target.value); setClienteInfo(null) }}
              />
            </div>
          )}

          {modo === 'cliente' && (
            <Button variant="secondary" onClick={resolverCliente}>
              Buscar cliente
            </Button>
          )}
          <Button onClick={cargarInscripciones} disabled={modo === 'cliente' && !clienteInfo}>
            Consultar
          </Button>
        </div>
        {clienteInfo && (
          <p className="mt-3 text-xs font-medium text-green-600">
            Cliente: {clienteInfo.nombre || 'Sin nombre'} ({clienteInfo.dni})
          </p>
        )}
      </Card>

      <Card>
        <Table headers={['ID', 'Cliente', 'Actividad', 'Fecha inscripción', 'Acciones']}>
          {inscripciones.length === 0 ? (
            <tr>
              <td colSpan={5}>
                <EmptyState message="Sin inscripciones para la consulta seleccionada." />
              </td>
            </tr>
          ) : (
            inscripciones.map((ins) => (
              <tr key={ins.id}>
                <td className="px-4 py-3">#{ins.id}</td>
                <td className="px-4 py-3 text-slate-600">
                  {modo === 'cliente' && clienteInfo ? clienteInfo.nombre : `#${ins.clienteId}`}
                </td>
                <td className="px-4 py-3 text-slate-600">
                  {modo === 'actividad'
                    ? actividades.find((a) => a.id === ins.actividadId)?.nombre
                    : `#${ins.actividadId}`}
                </td>
                <td className="px-4 py-3 text-slate-600">{formatoFechaHora(ins.fechaInscripcion)}</td>
                <td className="px-4 py-3">
                  <Button variant="danger" size="sm" onClick={() => setAnular(ins)}>
                    Anular
                  </Button>
                </td>
              </tr>
            ))
          )}
        </Table>
      </Card>

      {mostrarForm && (
        <NuevaInscripcionForm
          actividades={actividades}
          onClose={() => setMostrarForm(false)}
          onGuardado={() => { setMostrarForm(false); cargarInscripciones() }}
        />
      )}

      <ConfirmDialog
        open={!!anular}
        onClose={() => setAnular(null)}
        onConfirm={confirmarAnular}
        title="Anular inscripción"
        message={`¿Seguro que quieres anular la inscripción #${anular?.id}?`}
      />
    </div>
  )
}

function NuevaInscripcionForm({ actividades, onClose, onGuardado }) {
  const [dniCliente, setDniCliente] = useState('')
  const [clienteId, setClienteId] = useState(null)
  const [actividadId, setActividadId] = useState('')
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const resolverCliente = async () => {
    if (!dniCliente.trim()) return
    try {
      const cliente = await clientesApi.obtenerPorDni(dniCliente.trim())
      setClienteId(cliente.id)
      setError(null)
    } catch (err) {
      setClienteId(null)
      setError(`Cliente no encontrado: ${err.message}`)
    }
  }

  const guardar = async (e) => {
    e.preventDefault()
    if (!clienteId) {
      setError('Busca primero un cliente por DNI')
      return
    }
    if (!actividadId) {
      setError('Selecciona una actividad')
      return
    }
    setError(null)
    setEnviando(true)
    try {
      await inscripcionesApi.inscribir(clienteId, Number(actividadId))
      onGuardado()
    } catch (err) {
      setError(err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Modal open onClose={onClose} title="Nueva inscripción">
      <form onSubmit={guardar} className="space-y-4">
        {error && <Alert>{error}</Alert>}
        <div className="flex items-end gap-2">
          <div className="flex-1">
            <Input
              label="DNI del cliente"
              placeholder="12345678A"
              value={dniCliente}
              onChange={(e) => { setDniCliente(e.target.value); setClienteId(null) }}
            />
          </div>
          <Button type="button" variant="secondary" onClick={resolverCliente}>
            Buscar
          </Button>
        </div>
        {clienteId && <p className="text-xs font-medium text-green-600">Cliente encontrado ✓</p>}

        <Select
          label="Actividad"
          required
          value={actividadId}
          onChange={(e) => setActividadId(e.target.value)}
        >
          <option value="">Selecciona una actividad…</option>
          {actividades
            .filter((a) => a.estado === 'PLANIFICADA')
            .map((a) => (
              <option key={a.id} value={a.id}>
                {a.nombre}
              </option>
            ))}
        </Select>

        <div className="flex justify-end gap-2">
          <Button variant="secondary" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" disabled={enviando}>
            {enviando ? 'Inscribiendo…' : 'Inscribir'}
          </Button>
        </div>
      </form>
    </Modal>
  )
}