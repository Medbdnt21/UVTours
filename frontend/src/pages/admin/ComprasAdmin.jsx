import { useEffect, useState } from 'react'
import {
  comprasApi,
  viajesApi,
  tiendasApi,
  empleadosApi,
  clientesApi,
  formatoMoneda,
  formatoFechaHora,
} from '../../api/client.js'
import { Card, PageHeader, Spinner, Alert, Table, Badge, EmptyState, Button, Modal, Input, Select } from '../../components/ui.jsx'

export default function ComprasAdmin() {
  const [compras, setCompras] = useState([])
  const [viajes, setViajes] = useState([])
  const [tiendas, setTiendas] = useState([])
  const [empleados, setEmpleados] = useState([])
  const [clientes, setClientes] = useState({}) // id -> dni
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(null)
  const [mostrarForm, setMostrarForm] = useState(false)

  const cargar = async () => {
    try {
      const [listaCompras, listaViajes, listaTiendas, listaEmpleados] = await Promise.all([
        comprasApi.listar(),
        viajesApi.listar(),
        tiendasApi.listar(),
        empleadosApi.listar(),
      ])
      setCompras(listaCompras)
      setViajes(listaViajes)
      setTiendas(listaTiendas)
      setEmpleados(listaEmpleados)

      const ids = [...new Set(listaCompras.map((c) => c.clienteId).filter(Boolean))]
      const resultados = await Promise.allSettled(ids.map((id) => clientesApi.obtener(id)))
      const mapa = {}
      resultados.forEach((r, i) => {
        if (r.status === 'fulfilled') mapa[r.value.id] = r.value.dni
      })
      setClientes(mapa)
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

  const nombreViaje = (id) => viajes.find((v) => v.id === id)?.nombre || `#${id}`
  const direccionTienda = (id) => tiendas.find((t) => t.id === id)?.direccion || `#${id}`
  const nombreEmpleado = (id) => empleados.find((e) => e.id === id)?.nombre || `#${id}`

  if (cargando) return <Spinner />

  return (
    <div>
      <PageHeader title="Gestión de compras" subtitle="Compras registradas en tienda y online">
        <Button onClick={() => setMostrarForm(true)}>+ Comprar en tienda</Button>
      </PageHeader>

      {error && (
        <div className="mb-4">
          <Alert>{error}</Alert>
        </div>
      )}

      <Card>
        <Table headers={['Viaje', 'Cliente', 'Tienda', 'Empleado', 'Fecha', 'Precio', 'Origen']}>
          {compras.length === 0 ? (
            <tr>
              <td colSpan={7}>
                <EmptyState message="No hay compras registradas." />
              </td>
            </tr>
          ) : (
            compras.map((c) => (
              <tr key={c.id}>
                <td className="px-4 py-3 font-medium text-slate-900">{nombreViaje(c.viajeId)}</td>
                <td className="px-4 py-3 text-slate-600">{clientes[c.clienteId] || `#${c.clienteId}`}</td>
                <td className="px-4 py-3 text-slate-600">
                  {c.compraOnline ? '—' : direccionTienda(c.tiendaId)}
                </td>
                <td className="px-4 py-3 text-slate-600">
                  {c.compraOnline ? '—' : c.empleadoId ? nombreEmpleado(c.empleadoId) : '—'}
                </td>
                <td className="px-4 py-3 text-slate-600">{formatoFechaHora(c.fechaCompra)}</td>
                <td className="px-4 py-3 font-semibold">{formatoMoneda(c.precioFinal)}</td>
                <td className="px-4 py-3">
                  {c.compraOnline ? <Badge color="sky">Online</Badge> : <Badge color="slate">Tienda</Badge>}
                </td>
              </tr>
            ))
          )}
        </Table>
      </Card>

      {mostrarForm && (
        <CompraTiendaForm
          viajes={viajes}
          tiendas={tiendas}
          empleados={empleados}
          onClose={() => setMostrarForm(false)}
          onGuardado={() => { cargar(); setMostrarForm(false) }}
        />
      )}
    </div>
  )
}

function CompraTiendaForm({ viajes, tiendas, empleados, onClose, onGuardado }) {
  const [dniCliente, setDniCliente] = useState('')
  const [clienteId, setClienteId] = useState(null)
  const [viajeId, setViajeId] = useState('')
  const [tiendaId, setTiendaId] = useState('')
  const [empleadoId, setEmpleadoId] = useState('')
  const [acompanantes, setAcompanantes] = useState([])
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
    if (!viajeId) {
      setError('Selecciona un viaje')
      return
    }
    if (!tiendaId) {
      setError('Selecciona una tienda')
      return
    }
    setError(null)
    setEnviando(true)
    try {
      await comprasApi.realizar({
        clienteId,
        viajeId: Number(viajeId),
        tiendaId: Number(tiendaId),
        empleadoId: empleadoId ? Number(empleadoId) : null,
        acompanantes: acompanantes.filter((a) => a.nombre.trim() !== ''),
      })
      onGuardado()
    } catch (err) {
      setError(err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Modal open onClose={onClose} title="Compra en tienda">
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
        {clienteId && (
          <p className="text-xs font-medium text-green-600">Cliente encontrado ✓</p>
        )}

        <Select label="Viaje" required value={viajeId} onChange={(e) => setViajeId(e.target.value)}>
          <option value="">Selecciona un viaje…</option>
          {viajes.map((v) => (
            <option key={v.id} value={v.id}>
              {v.nombre} — {formatoMoneda(v.precio)}
            </option>
          ))}
        </Select>

        <Select label="Tienda" required value={tiendaId} onChange={(e) => setTiendaId(e.target.value)}>
          <option value="">Selecciona una tienda…</option>
          {tiendas.map((t) => (
            <option key={t.id} value={t.id}>
              {t.direccion}
            </option>
          ))}
        </Select>

        <Select
          label="Empleado (opcional)"
          value={empleadoId}
          onChange={(e) => setEmpleadoId(e.target.value)}
        >
          <option value="">Sin empleado</option>
          {empleados.map((em) => (
            <option key={em.id} value={em.id}>
              {em.nombre}
            </option>
          ))}
        </Select>

        <div>
          <div className="mb-2 flex items-center justify-between">
            <span className="text-sm font-medium text-slate-700">Acompañantes</span>
            <Button
              variant="secondary"
              size="sm"
              type="button"
              onClick={() => setAcompanantes([...acompanantes, { nombre: '', dni: '' }])}
            >
              + Añadir
            </Button>
          </div>
          {acompanantes.map((a, i) => (
            <div key={i} className="mb-2 flex items-center gap-2">
              <Input
                placeholder="Nombre"
                value={a.nombre}
                onChange={(e) =>
                  setAcompanantes(acompanantes.map((x, j) => (j === i ? { ...x, nombre: e.target.value } : x)))
                }
              />
              <Input
                placeholder="DNI"
                value={a.dni}
                onChange={(e) =>
                  setAcompanantes(acompanantes.map((x, j) => (j === i ? { ...x, dni: e.target.value } : x)))
                }
              />
              <button
                type="button"
                className="rounded-lg p-2 text-slate-400 hover:bg-red-50 hover:text-red-600"
                onClick={() => setAcompanantes(acompanantes.filter((_, j) => j !== i))}
              >
                ✕
              </button>
            </div>
          ))}
        </div>

        <div className="flex justify-end gap-2">
          <Button variant="secondary" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" disabled={enviando}>
            {enviando ? 'Registrando…' : 'Registrar compra'}
          </Button>
        </div>
      </form>
    </Modal>
  )
}