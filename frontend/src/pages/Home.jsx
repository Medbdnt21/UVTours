import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext.jsx'
import { viajesApi, actividadesApi, inscripcionesApi, formatoMoneda, formatoFecha } from '../api/client.js'
import { Spinner, Card, Badge, Alert, EstadoBadge, TipoViajeBadge, EmptyState, Button } from '../components/ui.jsx'
import CompraModal from '../components/CompraModal.jsx'

export default function Home() {
  const { usuario, esCliente } = useAuth()
  const [viajes, setViajes] = useState([])
  const [actividadesHoy, setActividadesHoy] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState(null)
  const [compraViaje, setCompraViaje] = useState(null)

  useEffect(() => {
    const cargar = async () => {
      try {
        const [listaViajes, listaHoy] = await Promise.all([
          viajesApi.listar(),
          actividadesApi.hoy(),
        ])
        setViajes(listaViajes)
        setActividadesHoy(listaHoy)
      } catch (err) {
        setError(err.message)
      } finally {
        setCargando(false)
      }
    }
    cargar()
  }, [])

  if (cargando) return <Spinner />
  if (error)
    return (
      <div className="mx-auto max-w-2xl">
        <Alert>{error}</Alert>
      </div>
    )

  return (
    <div className="space-y-12">
      <section className="rounded-2xl bg-gradient-to-r from-sky-600 to-teal-500 px-8 py-12 text-white shadow-lg">
        <h1 className="text-3xl font-bold sm:text-4xl">Viaja con UvTours</h1>
        <p className="mt-3 max-w-2xl text-sky-100">
          Descubre nuestros viajes simples y circuitos, con actividades para cada día.
          {esCliente ? ' Compra online y gestiona tus inscripciones desde tu cuenta.' : ' Crea tu cuenta para comprar online.'}
        </p>
      </section>

      <section>
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-xl font-bold text-slate-900">Catálogo de viajes</h2>
          <span className="text-sm text-slate-500">{viajes.length} viajes</span>
        </div>
        {viajes.length === 0 ? (
          <Card>
            <EmptyState message="Todavía no hay viajes publicados." />
          </Card>
        ) : (
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {viajes.map((viaje) => (
              <ViajeCard key={viaje.id} viaje={viaje} onComprar={esCliente ? () => setCompraViaje(viaje) : null} />
            ))}
          </div>
        )}
      </section>

      <section>
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-xl font-bold text-slate-900">Actividades de hoy</h2>
          <span className="text-sm text-slate-500">{actividadesHoy.length} actividades</span>
        </div>
        {actividadesHoy.length === 0 ? (
          <Card>
            <EmptyState message="No hay actividades programadas para hoy." />
          </Card>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {actividadesHoy.map((act) => (
              <Card key={act.id} className="p-5">
                <div className="flex items-start justify-between gap-2">
                  <h3 className="font-semibold text-slate-900">{act.nombre}</h3>
                  <EstadoBadge estado={act.estado} />
                </div>
                <p className="mt-1 text-sm text-slate-500">{formatoFecha(act.fecha)}</p>
                {esCliente && <InscripcionBoton actividadId={act.actividadId} />}
              </Card>
            ))}
          </div>
        )}
      </section>

      {compraViaje && (
        <CompraModal viaje={compraViaje} onClose={() => setCompraViaje(null)} />
      )}
    </div>
  )
}

function ViajeCard({ viaje, onComprar }) {
  const precioFinal =
    viaje.esLunaMiel && Number(viaje.descuentoLunaMiel) > 0
      ? Number(viaje.precio) - Number(viaje.descuentoLunaMiel)
      : Number(viaje.precio)

  return (
    <Card className="flex flex-col p-5">
      <div className="flex items-center justify-between gap-2">
        <TipoViajeBadge tipo={viaje.tipoViaje} />
        {viaje.esLunaMiel && <Badge color="amber">Luna de miel</Badge>}
      </div>
      <h3 className="mt-3 text-lg font-bold text-slate-900">{viaje.nombre}</h3>
      <p className="mt-1 text-sm text-slate-500">
        {formatoFecha(viaje.fechaInicio)} → {formatoFecha(viaje.fechaFin)}
      </p>
      {viaje.tipoViaje === 'SIMPLE' && (
        <p className="mt-1 text-sm text-slate-500">Destino: {viaje.destino}</p>
      )}
      <div className="mt-auto pt-4">
        <div className="flex items-baseline gap-2">
          <span className="text-xl font-bold text-slate-900">
            {formatoMoneda(precioFinal)}
          </span>
          {Number(viaje.descuentoLunaMiel) > 0 && (
            <span className="text-sm text-slate-400 line-through">{formatoMoneda(viaje.precio)}</span>
          )}
        </div>
        {onComprar && (
          <Button onClick={onComprar} className="mt-3 w-full">
            Comprar
          </Button>
        )}
      </div>
    </Card>
  )
}

function InscripcionBoton({ actividadId }) {
  const [mensaje, setMensaje] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const inscribirse = async () => {
    setEnviando(true)
    setMensaje(null)
    try {
      await inscripcionesApi.inscribirMia(actividadId)
      setMensaje({ tipo: 'success', texto: '¡Inscripción realizada!' })
    } catch (err) {
      setMensaje({ tipo: 'error', texto: err.message })
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="mt-3">
      <Button variant="secondary" size="sm" onClick={inscribirse} disabled={enviando}>
        {enviando ? 'Inscribiendo…' : 'Inscribirme'}
      </Button>
      {mensaje && (
        <p className={`mt-2 text-xs ${mensaje.tipo === 'success' ? 'text-green-600' : 'text-red-600'}`}>
          {mensaje.texto}
        </p>
      )}
    </div>
  )
}