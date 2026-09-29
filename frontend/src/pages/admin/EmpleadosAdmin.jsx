import { useEffect, useState } from 'react'
import { empleadosApi, tiendasApi } from '../../api/client.js'
import { Card, PageHeader, Spinner, Alert, Table, EmptyState, Button, Modal, Input, Select, ConfirmDialog } from '../../components/ui.jsx'
import { useAuth } from '../../context/AuthContext.jsx'

export default function EmpleadosAdmin() {
  const { esAdmin } = useAuth()
  const [empleados, setEmpleados] = useState([])
  const [tiendas, setTiendas] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(null)
  const [mostrarForm, setMostrarForm] = useState(false)
  const [editando, setEditando] = useState(null)
  const [eliminar, setEliminar] = useState(null)

  const cargar = async () => {
    try {
      const [listaEmpleados, listaTiendas] = await Promise.all([
        empleadosApi.listar(),
        tiendasApi.listar(),
      ])
      setEmpleados(listaEmpleados)
      setTiendas(listaTiendas)
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

  const nombreTienda = (id) => tiendas.find((t) => t.id === id)?.direccion || 'Sin tienda'

  const confirmarEliminar = async () => {
    try {
      await empleadosApi.eliminar(eliminar.id)
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
      <PageHeader title="Gestión de empleados" subtitle="Personal de las tiendas">
        {esAdmin && (
          <Button onClick={() => { setEditando(null); setMostrarForm(true) }}>
            + Nuevo empleado
          </Button>
        )}
      </PageHeader>

      {error && (
        <div className="mb-4">
          <Alert>{error}</Alert>
        </div>
      )}

      <Card>
        <Table headers={['Nombre', 'Email', 'DNI', 'Tienda', 'Acciones']}>
          {empleados.length === 0 ? (
            <tr>
              <td colSpan={5}>
                <EmptyState message="No hay empleados registrados." />
              </td>
            </tr>
          ) : (
            empleados.map((e) => (
              <tr key={e.id}>
                <td className="px-4 py-3 font-medium text-slate-900">{e.nombre}</td>
                <td className="px-4 py-3 text-slate-600">{e.email}</td>
                <td className="px-4 py-3 text-slate-600">{e.dni}</td>
                <td className="px-4 py-3 text-slate-600">{nombreTienda(e.tiendaId)}</td>
                <td className="px-4 py-3">
                  {esAdmin && (
                    <div className="flex gap-1.5">
                      <Button variant="secondary" size="sm" onClick={() => { setEditando(e); setMostrarForm(true) }}>
                        Editar
                      </Button>
                      <Button variant="danger" size="sm" onClick={() => setEliminar(e)}>
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
        <EmpleadoForm
          empleado={editando}
          tiendas={tiendas}
          onClose={() => setMostrarForm(false)}
          onGuardado={cargar}
        />
      )}

      <ConfirmDialog
        open={!!eliminar}
        onClose={() => setEliminar(null)}
        onConfirm={confirmarEliminar}
        title="Eliminar empleado"
        message={`¿Seguro que quieres eliminar a «${eliminar?.nombre}»?`}
      />
    </div>
  )
}

function EmpleadoForm({ empleado, tiendas, onClose, onGuardado }) {
  const esEdicion = !!empleado
  const [form, setForm] = useState({
    nombre: empleado?.nombre ?? '',
    email: empleado?.email ?? '',
    dni: empleado?.dni ?? '',
    tiendaId: empleado?.tiendaId ?? '',
  })
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const cambiar = (campo) => (e) => setForm({ ...form, [campo]: e.target.value })

  const guardar = async (e) => {
    e.preventDefault()
    if (!form.nombre.trim()) {
      setError('El nombre es obligatorio')
      return
    }
    if (!form.tiendaId) {
      setError('Selecciona una tienda')
      return
    }
    setError(null)
    setEnviando(true)
    try {
      const payload = { ...form, tiendaId: Number(form.tiendaId) }
      if (esEdicion) await empleadosApi.actualizar(empleado.id, payload)
      else await empleadosApi.crear(payload)
      onGuardado()
      onClose()
    } catch (err) {
      setError(err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Modal open onClose={onClose} title={esEdicion ? 'Editar empleado' : 'Nuevo empleado'}>
      <form onSubmit={guardar} className="space-y-4">
        {error && <Alert>{error}</Alert>}
        <Input label="Nombre" required value={form.nombre} onChange={cambiar('nombre')} />
        <Input label="Email" type="email" value={form.email} onChange={cambiar('email')} />
        <Input label="DNI" value={form.dni} onChange={cambiar('dni')} />
        <Select label="Tienda" required value={form.tiendaId} onChange={cambiar('tiendaId')}>
          <option value="">Selecciona una tienda…</option>
          {tiendas.map((t) => (
            <option key={t.id} value={t.id}>
              {t.direccion}
            </option>
          ))}
        </Select>
        <div className="flex justify-end gap-2">
          <Button variant="secondary" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" disabled={enviando}>
            {enviando ? 'Guardando…' : esEdicion ? 'Guardar cambios' : 'Crear empleado'}
          </Button>
        </div>
      </form>
    </Modal>
  )
}