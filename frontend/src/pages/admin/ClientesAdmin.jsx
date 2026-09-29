import { useState } from 'react'
import { clientesApi } from '../../api/client.js'
import { Card, PageHeader, Alert, Button, Input, Modal, EmptyState, Spinner, ConfirmDialog } from '../../components/ui.jsx'

export default function ClientesAdmin() {
  const [cliente, setCliente] = useState(null)
  const [buscando, setBuscando] = useState(false)
  const [busquedaDni, setBusquedaDni] = useState('')
  const [error, setError] = useState(null)
  const [mostrarForm, setMostrarForm] = useState(false)
  const [editando, setEditando] = useState(null)
  const [eliminar, setEliminar] = useState(null)
  const [noEncontrado, setNoEncontrado] = useState(false)

  const buscar = async (e) => {
    e.preventDefault()
    if (!busquedaDni.trim()) return
    setBuscando(true)
    setError(null)
    setNoEncontrado(false)
    setCliente(null)
    try {
      const resultado = await clientesApi.obtenerPorDni(busquedaDni.trim())
      setCliente(resultado)
    } catch (err) {
      setNoEncontrado(true)
      setError(err.message)
    } finally {
      setBuscando(false)
    }
  }

  const confirmarEliminar = async () => {
    try {
      await clientesApi.eliminar(eliminar.id)
      setEliminar(null)
      setCliente(null)
    } catch (err) {
      setError(err.message)
      setEliminar(null)
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader title="Gestión de clientes" subtitle="Busca por DNI, crea o edita clientes">
        <Button onClick={() => { setEditando(null); setMostrarForm(true) }}>
          + Nuevo cliente
        </Button>
      </PageHeader>

      {error && (
        <div className="mb-4">
          <Alert>{error}</Alert>
        </div>
      )}

      <Card className="p-5">
        <form onSubmit={buscar} className="flex flex-wrap items-end gap-3">
          <div className="min-w-64 flex-1">
            <Input
              label="Buscar por DNI"
              placeholder="Ej. 12345678A"
              value={busquedaDni}
              onChange={(e) => setBusquedaDni(e.target.value)}
            />
          </div>
          <Button type="submit" disabled={buscando}>
            {buscando ? 'Buscando…' : 'Buscar'}
          </Button>
        </form>
      </Card>

      {noEncontrado && (
        <Card>
          <EmptyState message="No se encontró ningún cliente con ese DNI." />
        </Card>
      )}

      {cliente && (
        <Card className="p-5">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <h3 className="text-lg font-bold text-slate-900">{cliente.nombre || 'Sin nombre'}</h3>
              <dl className="mt-2 grid gap-1 text-sm text-slate-600 sm:grid-cols-2">
                <div>
                  <dt className="inline font-medium">DNI:</dt> <dd className="inline">{cliente.dni}</dd>
                </div>
                <div>
                  <dt className="inline font-medium">Email:</dt> <dd className="inline">{cliente.email || '—'}</dd>
                </div>
                <div>
                  <dt className="inline font-medium">Teléfono:</dt> <dd className="inline">{cliente.telefono || '—'}</dd>
                </div>
                <div>
                  <dt className="inline font-medium">Dirección:</dt> <dd className="inline">{cliente.direccion || '—'}</dd>
                </div>
                <div>
                  <dt className="inline font-medium">Acompañantes:</dt>{' '}
                  <dd className="inline">{cliente.acompanantes?.length ?? 0}</dd>
                </div>
              </dl>
            </div>
            <div className="flex gap-2">
              <Button variant="secondary" onClick={() => { setEditando(cliente); setMostrarForm(true) }}>
                Editar
              </Button>
              <Button variant="danger" onClick={() => setEliminar(cliente)}>
                Eliminar
              </Button>
            </div>
          </div>
        </Card>
      )}

      {mostrarForm && (
        <ClienteForm cliente={editando} onClose={() => setMostrarForm(false)} onGuardado={(c) => setCliente(c)} />
      )}

      <ConfirmDialog
        open={!!eliminar}
        onClose={() => setEliminar(null)}
        onConfirm={confirmarEliminar}
        title="Eliminar cliente"
        message={`¿Seguro que quieres eliminar al cliente «${eliminar?.nombre || eliminar?.dni}»?`}
      />
    </div>
  )
}

function ClienteForm({ cliente, onClose, onGuardado }) {
  const esEdicion = !!cliente
  const [form, setForm] = useState({
    dni: cliente?.dni ?? '',
    nombre: cliente?.nombre ?? '',
    email: cliente?.email ?? '',
    telefono: cliente?.telefono ?? '',
    direccion: cliente?.direccion ?? '',
  })
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const cambiar = (campo) => (e) => setForm({ ...form, [campo]: e.target.value })

  const guardar = async (e) => {
    e.preventDefault()
    if (!form.dni.trim()) {
      setError('El DNI es obligatorio')
      return
    }
    setError(null)
    setEnviando(true)
    try {
      let resultado
      if (esEdicion) resultado = await clientesApi.actualizar(cliente.id, form)
      else resultado = await clientesApi.crear(form)
      onGuardado(resultado)
      onClose()
    } catch (err) {
      setError(err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Modal open onClose={onClose} title={esEdicion ? 'Editar cliente' : 'Nuevo cliente'}>
      <form onSubmit={guardar} className="space-y-4">
        {error && <Alert>{error}</Alert>}
        <Input label="DNI" required value={form.dni} onChange={cambiar('dni')} disabled={esEdicion} />
        <Input label="Nombre" value={form.nombre} onChange={cambiar('nombre')} />
        <Input label="Email" type="email" value={form.email} onChange={cambiar('email')} />
        <Input label="Teléfono" value={form.telefono} onChange={cambiar('telefono')} />
        <Input label="Dirección" value={form.direccion} onChange={cambiar('direccion')} />
        <div className="flex justify-end gap-2">
          <Button variant="secondary" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" disabled={enviando}>
            {enviando ? 'Guardando…' : esEdicion ? 'Guardar cambios' : 'Crear cliente'}
          </Button>
        </div>
      </form>
    </Modal>
  )
}