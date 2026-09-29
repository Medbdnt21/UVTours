import { useEffect, useState } from 'react'
import { viajesApi, formatoMoneda, formatoFecha } from '../../api/client.js'
import { Card, PageHeader, Spinner, Alert, Table, Badge, TipoViajeBadge, EmptyState, Button, ConfirmDialog } from '../../components/ui.jsx'
import ViajeForm from './ViajeForm.jsx'

export default function ViajesAdmin() {
  const [viajes, setViajes] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(null)
  const [mostrarForm, setMostrarForm] = useState(false)
  const [editando, setEditando] = useState(null)
  const [eliminar, setEliminar] = useState(null)
  const [descuento, setDescuento] = useState(null)

  const cargar = async () => {
    try {
      setViajes(await viajesApi.listar())
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
      await viajesApi.eliminar(eliminar.id)
      setEliminar(null)
      cargar()
    } catch (err) {
      setError(err.message)
      setEliminar(null)
    }
  }

  const guardarDescuento = async () => {
    try {
      await viajesApi.descuentoLunaMiel(descuento.id, Number(descuento.valor) || 0)
      setDescuento(null)
      cargar()
    } catch (err) {
      setError(err.message)
      setDescuento(null)
    }
  }

  const viajesSimples = viajes.filter((v) => v.tipoViaje === 'SIMPLE')

  if (cargando) return <Spinner />

  return (
    <div>
      <PageHeader title="Gestión de viajes" subtitle="Administra viajes simples y circuitos">
        <Button onClick={() => { setEditando(null); setMostrarForm(true) }}>
          + Nuevo viaje
        </Button>
      </PageHeader>

      {error && (
        <div className="mb-4">
          <Alert>{error}</Alert>
        </div>
      )}

      <Card>
        <Table
          headers={['Nombre', 'Tipo', 'Fechas', 'Destino / Componentes', 'Precio', 'Luna de miel', 'Acciones']}
        >
          {viajes.length === 0 ? (
            <tr>
              <td colSpan={7}>
                <EmptyState message="No hay viajes registrados." />
              </td>
            </tr>
          ) : (
            viajes.map((v) => (
              <tr key={v.id}>
                <td className="px-4 py-3 font-medium text-slate-900">{v.nombre}</td>
                <td className="px-4 py-3">
                  <TipoViajeBadge tipo={v.tipoViaje} />
                </td>
                <td className="px-4 py-3 text-slate-600">
                  {formatoFecha(v.fechaInicio)} → {formatoFecha(v.fechaFin)}
                </td>
                <td className="px-4 py-3 text-slate-600">
                  {v.tipoViaje === 'SIMPLE' ? (
                    v.destino
                  ) : (
                    <span>{v.viajesSimplesIds?.length ?? 0} viajes</span>
                  )}
                </td>
                <td className="px-4 py-3 font-semibold">{formatoMoneda(v.precio)}</td>
                <td className="px-4 py-3">
                  {v.esLunaMiel ? (
                    <Badge color="amber">
                      {Number(v.descuentoLunaMiel) > 0 ? `-${formatoMoneda(v.descuentoLunaMiel)}` : 'Sí'}
                    </Badge>
                  ) : (
                    <Badge color="slate">No</Badge>
                  )}
                </td>
                <td className="px-4 py-3">
                  <div className="flex gap-1.5">
                    <Button variant="secondary" size="sm" onClick={() => { setEditando(v); setMostrarForm(true) }}>
                      Editar
                    </Button>
                    {v.esLunaMiel && (
                      <Button
                        variant="secondary"
                        size="sm"
                        onClick={() => setDescuento({ id: v.id, valor: v.descuentoLunaMiel ?? '' })}
                      >
                        Descuento
                      </Button>
                    )}
                    <Button variant="danger" size="sm" onClick={() => setEliminar(v)}>
                      Eliminar
                    </Button>
                  </div>
                </td>
              </tr>
            ))
          )}
        </Table>
      </Card>

      {mostrarForm && (
        <ViajeForm
          viaje={editando}
          viajesSimples={viajesSimples}
          onClose={() => setMostrarForm(false)}
          onGuardado={cargar}
        />
      )}

      <ConfirmDialog
        open={!!eliminar}
        onClose={() => setEliminar(null)}
        onConfirm={confirmarEliminar}
        title="Eliminar viaje"
        message={`¿Seguro que quieres eliminar «${eliminar?.nombre}»? Esta acción no se puede deshacer.`}
      />

      <ConfirmDialog
        open={!!descuento}
        onClose={() => setDescuento(null)}
        onConfirm={guardarDescuento}
        title="Actualizar descuento de luna de miel"
        confirmLabel="Guardar"
        message={
          <div className="space-y-2">
            <p className="text-sm text-slate-600">
              Nuevo descuento para «{descuento ? viajes.find((v) => v.id === descuento.id)?.nombre : ''}»:
            </p>
            <input
              type="number"
              min="0"
              step="0.01"
              autoFocus
              value={descuento?.valor ?? ''}
              onChange={(e) => setDescuento({ ...descuento, valor: e.target.value })}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-500"
            />
          </div>
        }
      />
    </div>
  )
}