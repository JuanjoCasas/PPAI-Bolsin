import { useState } from 'react';
import {
  Card,
  CardContent,
  Typography,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  TableContainer,
  FormControl,
  FormLabel,
  RadioGroup,
  FormControlLabel,
  Radio,
  Button,
  Box,
  Checkbox,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  CircularProgress,
} from '@mui/material';
import { ArrowBack, CheckCircleOutline, Close } from '@mui/icons-material';
import { useRecepcion } from '../../context/RecepcionContext';
import { seleccionarOpcion, confirmar } from '../../services/bolsinService';

// grdBolsinSeleccionado + grdRemitos: mostrarDetalleBolsin()
// rdBtnOpcionesRecepcionBolsin: mostrarOpcionesRecepcionBolsin()
// btnConfirmarRecepcion + pedirConfirmacion()
const Paso2Detalle = () => {
  const { detalleBolsin, setMensajeResultado, setNotificaciones, nextStep, prevStep, resetWizard, setError, loading, setLoading } =
    useRecepcion();

  const [opcion, setOpcion] = useState('');
  const [marcadas, setMarcadas] = useState([]); // documentación marcada en A3, A4, A5
  const [localError, setLocalError] = useState('');
  const [confirmacion, setConfirmacion] = useState(null); // mensaje de pedirConfirmacion()

  if (!detalleBolsin) {
    return <Alert severity="warning">Primero seleccioná un bolsín.</Alert>;
  }

  // La primera opción es el flujo básico; las demás (A3, A4, A5) piden marcar documentación
  const [, ...opcionesAlternativas] = detalleBolsin.opcionesRecepcion;
  const esAlternativa = opcionesAlternativas.includes(opcion);
  const indicacion = {
    [opcionesAlternativas[0]]: 'Marcá la documentación que no llegó en el bolsín.',
    [opcionesAlternativas[1]]: 'Marcá la documentación que no corresponde a esta CM.',
    [opcionesAlternativas[2]]: 'Marcá la documentación que se debe redirigir a otra área.',
  }[opcion];

  const handleCambiarOpcion = (e) => {
    setOpcion(e.target.value);
    setMarcadas([]);
    setLocalError('');
  };

  const toggleMarcada = (numero) =>
    setMarcadas((prev) => (prev.includes(numero) ? prev.filter((n) => n !== numero) : [...prev, numero]));

  // seleccionaOpcionContenidoCoincide()
  const handleSeleccionarOpcion = async () => {
    setLoading(true);
    setLocalError('');
    const result = await seleccionarOpcion(opcion, esAlternativa ? marcadas : []);
    if (result.success) {
      setConfirmacion(result.data.mensaje);
    } else {
      setLocalError(result.message);
    }
    setLoading(false);
  };

  // confirmar()
  const handleConfirmar = async () => {
    setLoading(true);
    const result = await confirmar();
    setConfirmacion(null);
    if (result.success) {
      setMensajeResultado(result.data.mensaje);
      setNotificaciones(result.data.notificaciones ?? []);
      nextStep();
    } else {
      setError(result.message);
      setLocalError(result.message);
    }
    setLoading(false);
  };

  return (
    <Card>
      <CardContent sx={{ p: 4 }}>
        <Typography variant="h5" gutterBottom color="primary" fontWeight="bold">
          Bolsín precinto N° {detalleBolsin.numeroPrecinto}
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Origen: {detalleBolsin.cmOrigen}. Verificá que el contenido coincida con lo registrado.
        </Typography>

        <TableContainer sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 2, mb: 4 }}>
          <Table size="small">
            <TableHead>
              <TableRow>
                {esAlternativa && <TableCell padding="checkbox" />}
                <TableCell>Remito</TableCell>
                <TableCell>N° documentación</TableCell>
                <TableCell>Asunto</TableCell>
                <TableCell>Tipo</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {detalleBolsin.remitos.map((remito) =>
                remito.documentaciones.map((doc, i) => (
                  <TableRow key={`${remito.numero}-${doc.numero}`} selected={marcadas.includes(doc.numero)}>
                    {esAlternativa && (
                      <TableCell padding="checkbox">
                        <Checkbox
                          checked={marcadas.includes(doc.numero)}
                          onChange={() => toggleMarcada(doc.numero)}
                          inputProps={{ 'aria-label': `Marcar documentación ${doc.numero}` }}
                        />
                      </TableCell>
                    )}
                    {i === 0 && (
                      <TableCell rowSpan={remito.documentaciones.length} sx={{ fontWeight: 600 }}>
                        {remito.numero}
                      </TableCell>
                    )}
                    <TableCell>{doc.numero}</TableCell>
                    <TableCell>{doc.asunto}</TableCell>
                    <TableCell>{doc.tipo}</TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>

        <FormControl component="fieldset" fullWidth>
          <FormLabel component="legend" sx={{ mb: 1, fontWeight: 500 }}>
            Resultado de la recepción
          </FormLabel>
          <RadioGroup value={opcion} onChange={handleCambiarOpcion}>
            {detalleBolsin.opcionesRecepcion.map((op) => (
              <FormControlLabel key={op} value={op} control={<Radio />} label={op} />
            ))}
          </RadioGroup>
        </FormControl>

        {esAlternativa && (
          <Alert severity="info" sx={{ mt: 2 }}>
            {indicacion} La documentación sin marcar queda recibida y aceptada.
          </Alert>
        )}

        {localError && (
          <Alert severity="error" sx={{ mt: 3 }}>
            {localError}
          </Alert>
        )}

        <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 4 }}>
          <Button variant="outlined" startIcon={<ArrowBack />} onClick={prevStep} disabled={loading}>
            Anterior
          </Button>
          <Box sx={{ display: 'flex', gap: 2 }}>
          {/* Observación 1: el EB puede cancelar el caso de uso cuando lo decida */}
          <Button variant="outlined" color="secondary" startIcon={<Close />} onClick={resetWizard} disabled={loading}>
            Cancelar
          </Button>
          <Button
            variant="contained"
            endIcon={loading ? <CircularProgress size={18} color="inherit" /> : <CheckCircleOutline />}
            onClick={handleSeleccionarOpcion}
            disabled={!opcion || (esAlternativa && marcadas.length === 0) || loading}
            size="large"
          >
            Confirmar recepción
          </Button>
          </Box>
        </Box>
      </CardContent>

      {/* pedirConfirmacion() */}
      <Dialog open={!!confirmacion} onClose={() => setConfirmacion(null)}>
        <DialogTitle>Confirmar recepción</DialogTitle>
        <DialogContent>
          <DialogContentText>{confirmacion}</DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmacion(null)} disabled={loading}>
            Cancelar
          </Button>
          <Button variant="contained" onClick={handleConfirmar} disabled={loading}>
            Confirmar
          </Button>
        </DialogActions>
      </Dialog>
    </Card>
  );
};

export default Paso2Detalle;
