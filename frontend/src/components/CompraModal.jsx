import { useState } from 'react'
import { comprasApi, formatoMoneda, formatoFecha } from '../api/client.js'
import { Modal, Button, Input, Alert } from './ui.jsx'

export default function CompraModal({ viaje, onClose }) {
  const [acompanantes, setAcompanantes] = useState([])
  const [error, setError] = useState(null)
  const [resultado, setResultado] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const precioFinal =
    viaje.esLunaMiel && Number(viaje.descuentoLunaMiel) > 0
      ? Number(viaje.precio) - Number(viaje.descuentoLunaMiel)
      : Number(viaje.precio)

  const anadirAcompanante = () => {
    setAcompanantes([...acompanantes, { nombre: '', dni: '' }])
  }

  const cambiarAcompanante = (indice, campo, valor) => {
    setAcompanantes(
      acompanantes.map((a, i) => (i === indice ? { ...a, [campo]: valor } : a)),
    )
  }

  const quitarAcompanante = (indice) => {
    setAcompanantes(acompanantes.filter((_, i) => i !== indice))
  }

  const confirmar = async () => {
    setError(null)
    setEnviando(true)
    try {
      const compra = await comprasApi.realizarOnline({
        viajeId: viaje.id,
        acompanantes: acompanantes.filter((a) => a.nombre.trim() !== ''),
      })
      setResultado(compra)
    } catch (err) {
      setError(err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Modal open onClose={onClose} title="Confirmar compra online">
      {resultado ? (
        <div className="space-y-4">
          <Alert tipo="success">
            Compra realizada con éxito. Referencia de compra: #{resultado.id}
          </Alert>
          <div className="text-sm text-slate-600">
            <p>
              Viaje: <span className="font-medium">{viaje.nombre}</span>
            </p>
            <p>
              Precio final: <span className="font-semibold">{formatoMoneda(resultado.precioFinal)}</span>
            </p>
            <p>Acompañantes: {resultado.acompanantes?.length ?? 0}</p>
          </div>
          <Button variant="secondary" onClick={onClose} className="w-full">
            Cerrar
          </Button>
        </div>
      ) : (
        <div className="space-y-5">
          <div className="rounded-lg bg-slate-50 p-4 text-sm text-slate-600">
            <p className="font-semibold text-slate-900">{viaje.nombre}</p>
            <p className="mt-1">
              {formatoFecha(viaje.fechaInicio)} → {formatoFecha(viaje.fechaFin)}
            </p>
            <div className="mt-2 flex items-baseline gap-2">
              <span className="text-lg font-bold text-slate-900">{formatoMoneda(precioFinal)}</span>
              {Number(viaje.descuentoLunaMiel) > 0 && (
                <span className="text-sm text-slate-400 line-through">{formatoMoneda(viaje.precio)}</span>
              )}
            </div>
          </div>

          {error && <Alert>{error}</Alert>}

          <div>
            <div className="mb-2 flex items-center justify-between">
              <h3 className="text-sm font-medium text-slate-700">Acompañantes</h3>
              <Button variant="secondary" size="sm" onClick={anadirAcompanante}>
                + Añadir
              </Button>
            </div>
            {acompanantes.length === 0 && (
              <p className="text-xs text-slate-400">Sin acompañantes. Compra solo para ti.</p>
            )}
            <div className="space-y-3">
              {acompanantes.map((a, i) => (
                <div key={i} className="flex items-start gap-2">
                  <Input
                    placeholder="Nombre"
                    value={a.nombre}
                    onChange={(e) => cambiarAcompanante(i, 'nombre', e.target.value)}
                    className="flex-1"
                  />
                  <Input
                    placeholder="DNI"
                    value={a.dni}
                    onChange={(e) => cambiarAcompanante(i, 'dni', e.target.value)}
                    className="flex-1"
                  />
                  <button
                    onClick={() => quitarAcompanante(i)}
                    className="mt-1 rounded-lg p-2 text-slate-400 hover:bg-red-50 hover:text-red-600"
                    aria-label="Quitar acompañante"
                  >
                    <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
              ))}
            </div>
          </div>

          <div className="flex justify-end gap-2">
            <Button variant="secondary" onClick={onClose}>
              Cancelar
            </Button>
            <Button onClick={confirmar} disabled={enviando}>
              {enviando ? 'Procesando…' : `Confirmar ${formatoMoneda(precioFinal)}`}
            </Button>
          </div>
        </div>
      )}
    </Modal>
  )
}