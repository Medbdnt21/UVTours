import { useEffect, useState } from 'react'
import {
  comprasApi,
  inscripcionesApi,
  viajesApi,
  actividadesApi,
  formatoMoneda,
  formatoFecha,
  formatoFechaHora,
} from '../api/client.js'
import { Card, PageHeader, Spinner, Alert, Table, Badge, EmptyState, Button, ConfirmDialog } from '../components/ui.jsx'
import { useAuth } from '../context/AuthContext.jsx'

export default function MiCuenta() {
  const { usuario } = useAuth()
  const [compras, setCompras] = useState([])
  const [inscripciones, setInscripciones] = useState([])
  const [viajes, setViajes] = useState([])
  const [actividades, setActividades] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(null)
  const [anular, setAnular] = useState(null)
  const [mensaje, setMensaje] = useState(null)

  const cargar = async () => {
    try {
      const [listaCompras, listaInscripciones, listaViajes, listaActividades] = await Promise.all([
        comprasApi.misCompras(),
        inscripcionesApi.misInscripciones(),
        viajesApi.listar(),
        actividadesApi.listar(),
      ])
      setCompras(listaCompras)
      setInscripciones(listaInscripciones)
      setViajes(listaViajes)
      setActividades(listaActividades)
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
  const nombreActividad = (id) => actividades.find((a) => a.id === id)?.nombre || `#${id}`

  const confirmarAnulacion = async () => {
    try {
      await inscripcionesApi.anularMia(anular.id)
      setMensaje({ tipo: 'success', texto: 'Inscripción anulada.' })
      setAnular(null)
      cargar()
    } catch (err) {
      setMensaje({ tipo: 'error', texto: err.message })
      setAnular(null)
    }
  }

  if (cargando) return <Spinner />

  return (
    <div className="space-y-10">
      <PageHeader title={`Hola, ${usuario.nombre}`} subtitle="Tus compras e inscripciones" />

      {mensaje && (
        <Alert tipo={mensaje.tipo === 'success' ? 'success' : 'error'}>{mensaje.texto}</Alert>
      )}
      {error && !compras.length && (
        <div className="mb-4">
          <Alert>{error}</Alert>
        </div>
      )}

      <section>
        <h2 className="mb-4 text-lg font-bold text-slate-900">Mis compras</h2>
        {compras.length === 0 ? (
          <Card>
            <EmptyState message="Aún no has comprado ningún viaje." />
          </Card>
        ) : (
          <Card>
            <Table headers={['Viaje', 'Fecha', 'Online', 'Precio', 'Acompañantes']}>
              {compras.map((c) => (
                <tr key={c.id}>
                  <td className="px-4 py-3 font-medium text-slate-900">{nombreViaje(c.viajeId)}</td>
                  <td className="px-4 py-3">{formatoFechaHora(c.fechaCompra)}</td>
                  <td className="px-4 py-3">
                    {c.compraOnline ? <Badge color="sky">Online</Badge> : <Badge color="slate">Tienda</Badge>}
                  </td>
                  <td className="px-4 py-3 font-semibold">{formatoMoneda(c.precioFinal)}</td>
                  <td className="px-4 py-3">{c.acompanantes?.length ?? 0}</td>
                </tr>
              ))}
            </Table>
          </Card>
        )}
      </section>

      <section>
        <h2 className="mb-4 text-lg font-bold text-slate-900">Mis inscripciones</h2>
        {inscripciones.length === 0 ? (
          <Card>
            <EmptyState message="No estás inscrito en ninguna actividad." />
          </Card>
        ) : (
          <Card>
            <Table headers={['Actividad', 'Fecha inscripción', 'Acciones']}>
              {inscripciones.map((ins) => {
                const actividad = actividades.find((a) => a.id === ins.actividadId)
                return (
                  <tr key={ins.id}>
                    <td className="px-4 py-3 font-medium text-slate-900">
                      {nombreActividad(ins.actividadId)}
                      {actividad && (
                        <span className="ml-2 text-xs text-slate-400">
                          {formatoFecha(actividad.fechaActividad)}
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-3">{formatoFechaHora(ins.fechaInscripcion)}</td>
                    <td className="px-4 py-3">
                      <Button
                        variant="secondary"
                        size="sm"
                        onClick={() => setAnular(ins)}
                      >
                        Anular
                      </Button>
                    </td>
                  </tr>
                )
              })}
            </Table>
          </Card>
        )}
      </section>

      <ConfirmDialog
        open={!!anular}
        onClose={() => setAnular(null)}
        onConfirm={confirmarAnulacion}
        title="Anular inscripción"
        message={`¿Seguro que quieres anular tu inscripción en «${anular ? nombreActividad(anular.actividadId) : ''}»?`}
      />
    </div>
  )
}