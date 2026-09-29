import { useEffect, useState } from 'react'
import { tiendasApi } from '../../api/client.js'
import { Card, PageHeader, Spinner, Alert, Table, EmptyState, Button, Modal, Input, ConfirmDialog } from '../../components/ui.jsx'
import { useAuth } from '../../context/AuthContext.jsx'

export default function TiendasAdmin() {
  const { esAdmin } = useAuth()
  const [tiendas, setTiendas] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(null)
  const [mostrarForm, setMostrarForm] = useState(false)
  const [editando, setEditando] = useState(null)
  const [eliminar, setEliminar] = useState(null)

  const cargar = async () => {
    try {
      setTiendas(await tiendasApi.listar())
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

  const confirmarEliminar = async () => {
    try {
      await tiendasApi.eliminar(eliminar.id)
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
      <PageHeader title="Gestión de tiendas" subtitle="Oficinas físicas de la agencia">
        {esAdmin && (
          <Button onClick={() => { setEditando(null); setMostrarForm(true) }}>
            + Nueva tienda
          </Button>
        )}
      </PageHeader>

      {error && (
        <div className="mb-4">
          <Alert>{error}</Alert>
        </div>
      )}

      <Card>
        <Table headers={['Dirección', 'Código postal', 'Teléfono', 'Empleados', 'Acciones']}>
          {tiendas.length === 0 ? (
            <tr>
              <td colSpan={5}>
                <EmptyState message="No hay tiendas registradas." />
              </td>
            </tr>
          ) : (
            tiendas.map((t) => (
              <tr key={t.id}>
                <td className="px-4 py-3 font-medium text-slate-900">{t.direccion}</td>
                <td className="px-4 py-3 text-slate-600">{t.codigoPostal}</td>
                <td className="px-4 py-3 text-slate-600">{t.telefono}</td>
                <td className="px-4 py-3 text-slate-600">{t.empleados?.length ?? 0}</td>
                <td className="px-4 py-3">
                  {esAdmin && (
                    <div className="flex gap-1.5">
                      <Button variant="secondary" size="sm" onClick={() => { setEditando(t); setMostrarForm(true) }}>
                        Editar
                      </Button>
                      <Button variant="danger" size="sm" onClick={() => setEliminar(t)}>
                        Eliminar
                      </Button>
                    </div>
                  )}
                </td>
              </tr>
            ))
          )}
        </Table>
      </Card>

      {mostrarForm && (
        <TiendaForm tienda={editando} onClose={() => setMostrarForm(false)} onGuardado={cargar} />
      )}

      <ConfirmDialog
        open={!!eliminar}
        onClose={() => setEliminar(null)}
        onConfirm={confirmarEliminar}
        title="Eliminar tienda"
        message={`¿Seguro que quieres eliminar la tienda de «${eliminar?.direccion}»? No se puede eliminar si tiene compras asociadas.`}
      />
    </div>
  )
}

function TiendaForm({ tienda, onClose, onGuardado }) {
  const esEdicion = !!tienda
  const [form, setForm] = useState({
    direccion: tienda?.direccion ?? '',
    codigoPostal: tienda?.codigoPostal ?? '',
    telefono: tienda?.telefono ?? '',
  })
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const cambiar = (campo) => (e) => setForm({ ...form, [campo]: e.target.value })

  const guardar = async (e) => {
    e.preventDefault()
    if (!form.direccion.trim()) {
      setError('La dirección es obligatoria')
      return
    }
    setError(null)
    setEnviando(true)
    try {
      if (esEdicion) await tiendasApi.actualizar(tienda.id, form)
      else await tiendasApi.crear(form)
      onGuardado()
      onClose()
    } catch (err) {
      setError(err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Modal open onClose={onClose} title={esEdicion ? 'Editar tienda' : 'Nueva tienda'}>
      <form onSubmit={guardar} className="space-y-4">
        {error && <Alert>{error}</Alert>}
        <Input label="Dirección" required value={form.direccion} onChange={cambiar('direccion')} />
        <Input label="Código postal" value={form.codigoPostal} onChange={cambiar('codigoPostal')} />
        <Input label="Teléfono" value={form.telefono} onChange={cambiar('telefono')} />
        <div className="flex justify-end gap-2">
          <Button variant="secondary" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" disabled={enviando}>
            {enviando ? 'Guardando…' : esEdicion ? 'Guardar cambios' : 'Crear tienda'}
          </Button>
        </div>
      </form>
    </Modal>
  )
}