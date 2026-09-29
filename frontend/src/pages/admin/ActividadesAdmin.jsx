import { useEffect, useState } from 'react'
import { actividadesApi, formatoFecha } from '../../api/client.js'
import { Card, PageHeader, Spinner, Alert, Table, Badge, EstadoBadge, EmptyState, Button, ConfirmDialog, Modal, Input, Select } from '../../components/ui.jsx'
import { useAuth } from '../../context/AuthContext.jsx'

export default function ActividadesAdmin() {
  const { esAdmin } = useAuth()
  const [actividades, setActividades] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(null)
  const [mostrarForm, setMostrarForm] = useState(false)
  const [editando, setEditando] = useState(null)
  const [eliminar, setEliminar] = useState(null)

  const cargar = async () => {
    try {
      setActividades(await actividadesApi.listar())
      setError(null)
    } catch (err) {
      setError(err.message)
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => {
    cargar()
  }, [])

  const cambiarEstado = async (id, estado) => {
    try {
      await actividadesApi.cambiarEstado(id, estado)
      cargar()
    } catch (err) {
      setError(err.message)
    }
  }

  const confirmarEliminar = async () => {
    try {
      await actividadesApi.eliminar(eliminar.id)
      setEliminar(null)
      cargar()
    } catch (err) {
      setError(err.message)
      setEliminar(null)
    }
  }

  if (cargando) return <Spinner />

  return (
    <div>
      <PageHeader title="Gestión de actividades" subtitle="Crea actividades y controla su estado">
        {esAdmin && (
          <Button onClick={() => { setEditando(null); setMostrarForm(true) }}>
            + Nueva actividad
          </Button>
        )}
      </PageHeader>

      {error && (
        <div className="mb-4">
          <Alert>{error}</Alert>
        </div>
      )}

      <Card>
        <Table headers={['Nombre', 'Fecha', 'Estado', 'Acciones']}>
          {actividades.length === 0 ? (
            <tr>
              <td colSpan={4}>
                <EmptyState message="No hay actividades registradas." />
              </td>
            </tr>
          ) : (
            actividades.map((a) => (
              <tr key={a.id}>
                <td className="px-4 py-3 font-medium text-slate-900">{a.nombre}</td>
                <td className="px-4 py-3 text-slate-600">{formatoFecha(a.fechaActividad)}</td>
                <td className="px-4 py-3">
                  <EstadoBadge estado={a.estado} />
                </td>
                <td className="px-4 py-3">
                  <div className="flex flex-wrap gap-1.5">
                    {a.estado === 'PLANIFICADA' && (
                      <>
                        <Button variant="secondary" size="sm" onClick={() => cambiarEstado(a.id, 'EN_EJECUCION')}>
                          Iniciar
                        </Button>
                        <Button variant="secondary" size="sm" onClick={() => cambiarEstado(a.id, 'CANCELADA')}>
                          Cancelar
                        </Button>
                      </>
                    )}
                    {a.estado === 'EN_EJECUCION' && (
                      <Button variant="success" size="sm" onClick={() => cambiarEstado(a.id, 'FINALIZADA')}>
                        Finalizar
                      </Button>
                    )}
                    {esAdmin && (
                      <>
                        <Button variant="secondary" size="sm" onClick={() => { setEditando(a); setMostrarForm(true) }}>
                          Editar
                        </Button>
                        <Button variant="danger" size="sm" onClick={() => setEliminar(a)}>
                          Eliminar
                        </Button>
                      </>
                    )}
                  </div>
                </td>
              </tr>
            ))
          )}
        </Table>
      </Card>

      {mostrarForm && (
        <ActividadForm
          actividad={editando}
          onClose={() => setMostrarForm(false)}
          onGuardado={cargar}
        />
      )}

      <ConfirmDialog
        open={!!eliminar}
        onClose={() => setEliminar(null)}
        onConfirm={confirmarEliminar}
        title="Eliminar actividad"
        message={`¿Seguro que quieres eliminar «${eliminar?.nombre}»?`}
      />
    </div>
  )
}

function ActividadForm({ actividad, onClose, onGuardado }) {
  const esEdicion = !!actividad
  const [form, setForm] = useState({
    nombre: actividad?.nombre ?? '',
    fechaActividad: actividad?.fechaActividad ?? '',
  })
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const guardar = async (e) => {
    e.preventDefault()
    setError(null)
    setEnviando(true)
    try {
      if (esEdicion) await actividadesApi.actualizar(actividad.id, form)
      else await actividadesApi.crear(form)
      onGuardado()
      onClose()
    } catch (err) {
      setError(err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Modal open onClose={onClose} title={esEdicion ? 'Editar actividad' : 'Nueva actividad'}>
      <form onSubmit={guardar} className="space-y-4">
        {error && <Alert>{error}</Alert>}
        <Input
          label="Nombre"
          required
          value={form.nombre}
          onChange={(e) => setForm({ ...form, nombre: e.target.value })}
        />
        <Input
          label="Fecha"
          type="date"
          required
          value={form.fechaActividad}
          onChange={(e) => setForm({ ...form, fechaActividad: e.target.value })}
        />
        {esEdicion && (
          <div className="text-sm text-slate-500">
            Estado actual: <EstadoBadge estado={actividad.estado} />
          </div>
        )}
        <div className="flex justify-end gap-2">
          <Button variant="secondary" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" disabled={enviando}>
            {enviando ? 'Guardando…' : esEdicion ? 'Guardar cambios' : 'Crear actividad'}
          </Button>
        </div>
      </form>
    </Modal>
  )
}