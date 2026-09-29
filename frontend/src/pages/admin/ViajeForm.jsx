import { useState } from 'react'
import { viajesApi, formatoMoneda } from '../../api/client.js'
import { Modal, Button, Input, Select, Alert } from '../../components/ui.jsx'

export default function ViajeForm({ viaje, viajesSimples, onClose, onGuardado }) {
  const esEdicion = !!viaje

  const [form, setForm] = useState({
    nombre: viaje?.nombre ?? '',
    tipoViaje: viaje?.tipoViaje ?? 'SIMPLE',
    fechaInicio: viaje?.fechaInicio ?? '',
    fechaFin: viaje?.fechaFin ?? '',
    precio: viaje?.precio ?? '',
    esLunaMiel: viaje?.esLunaMiel ?? false,
    descuentoLunaMiel: viaje?.descuentoLunaMiel ?? '',
    destino: viaje?.destino ?? '',
    viajesSimplesIds: viaje?.viajesSimplesIds ?? [],
  })
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const cambiar = (campo) => (e) =>
    setForm({ ...form, [campo]: e.target.type === 'checkbox' ? e.target.checked : e.target.value })

  const toggleSimple = (id) => {
    setForm({
      ...form,
      viajesSimplesIds: form.viajesSimplesIds.includes(id)
        ? form.viajesSimplesIds.filter((x) => x !== id)
        : [...form.viajesSimplesIds, id],
    })
  }

  const validar = () => {
    if (!form.nombre.trim()) return 'El nombre es obligatorio'
    if (!form.fechaInicio || !form.fechaFin) return 'Las fechas son obligatorias'
    if (form.tipoViaje === 'SIMPLE' && !form.destino.trim()) return 'El destino es obligatorio'
    if (form.tipoViaje === 'CIRCUITO' && form.viajesSimplesIds.length === 0)
      return 'El circuito debe incluir al menos un viaje simple'
    return null
  }

  const guardar = async (e) => {
    e.preventDefault()
    const errorValidacion = validar()
    if (errorValidacion) {
      setError(errorValidacion)
      return
    }

    const payload = {
      nombre: form.nombre,
      tipoViaje: form.tipoViaje,
      fechaInicio: form.fechaInicio,
      fechaFin: form.fechaFin,
      precio: Number(form.precio) || 0,
      esLunaMiel: form.esLunaMiel,
      descuentoLunaMiel: form.esLunaMiel ? Number(form.descuentoLunaMiel) || 0 : 0,
      destino: form.tipoViaje === 'SIMPLE' ? form.destino : undefined,
      viajesSimplesIds: form.tipoViaje === 'CIRCUITO' ? form.viajesSimplesIds : undefined,
    }

    setError(null)
    setEnviando(true)
    try {
      if (esEdicion) await viajesApi.actualizar(viaje.id, payload)
      else await viajesApi.crear(payload)
      onGuardado()
      onClose()
    } catch (err) {
      setError(err.message)
    } finally {
      setEnviando(false)
    }
  }

  const esCircuito = form.tipoViaje === 'CIRCUITO'
  const precioTotal = viajesSimples
    .filter((v) => form.viajesSimplesIds.includes(v.id))
    .reduce((suma, v) => suma + Number(v.precio || 0), 0)

  return (
    <Modal open onClose={onClose} title={esEdicion ? 'Editar viaje' : 'Nuevo viaje'}>
      <form onSubmit={guardar} className="space-y-4">
        {error && <Alert>{error}</Alert>}

        <Select label="Tipo de viaje" value={form.tipoViaje} onChange={cambiar('tipoViaje')}>
          <option value="SIMPLE">Viaje simple</option>
          <option value="CIRCUITO">Circuito</option>
        </Select>

        <Input label="Nombre" required value={form.nombre} onChange={cambiar('nombre')} />

        {!esCircuito && (
          <Input label="Destino" required value={form.destino} onChange={cambiar('destino')} />
        )}

        <div className="grid grid-cols-2 gap-3">
          <Input label="Fecha inicio" type="date" required value={form.fechaInicio} onChange={cambiar('fechaInicio')} />
          <Input label="Fecha fin" type="date" required value={form.fechaFin} onChange={cambiar('fechaFin')} />
        </div>

        {!esCircuito ? (
          <Input
            label="Precio (€)"
            type="number"
            min="0"
            step="0.01"
            value={form.precio}
            onChange={cambiar('precio')}
          />
        ) : (
          <div className="rounded-lg bg-slate-50 p-3">
            <p className="mb-2 text-sm font-medium text-slate-700">
              Viajes simples que componen el circuito
            </p>
            {viajesSimples.length === 0 ? (
              <p className="text-xs text-slate-500">
                No hay viajes simples creados. Crea alguno primero.
              </p>
            ) : (
              <div className="space-y-1.5">
                {viajesSimples.map((v) => (
                  <label key={v.id} className="flex cursor-pointer items-center gap-2 text-sm">
                    <input
                      type="checkbox"
                      checked={form.viajesSimplesIds.includes(v.id)}
                      onChange={() => toggleSimple(v.id)}
                      className="h-4 w-4 rounded border-slate-300 text-sky-600"
                    />
                    <span className="flex-1">{v.nombre}</span>
                    <span className="text-xs text-slate-500">{formatoMoneda(v.precio)}</span>
                  </label>
                ))}
              </div>
            )}
            <p className="mt-3 text-xs text-slate-500">
              Precio total (se calcula en el servidor):{' '}
              <span className="font-semibold text-slate-700">{formatoMoneda(precioTotal)}</span>
            </p>
          </div>
        )}

        <label className="flex cursor-pointer items-center gap-2">
          <input
            type="checkbox"
            checked={form.esLunaMiel}
            onChange={cambiar('esLunaMiel')}
            className="h-4 w-4 rounded border-slate-300 text-sky-600"
          />
          <span className="text-sm font-medium text-slate-700">Viaje de luna de miel</span>
        </label>

        {form.esLunaMiel && (
          <Input
            label="Descuento luna de miel (€)"
            type="number"
            min="0"
            step="0.01"
            value={form.descuentoLunaMiel}
            onChange={cambiar('descuentoLunaMiel')}
          />
        )}

        <div className="flex justify-end gap-2 pt-2">
          <Button variant="secondary" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" disabled={enviando}>
            {enviando ? 'Guardando…' : esEdicion ? 'Guardar cambios' : 'Crear viaje'}
          </Button>
        </div>
      </form>
    </Modal>
  )
}