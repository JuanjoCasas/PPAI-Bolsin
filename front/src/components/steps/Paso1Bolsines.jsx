import { useEffect, useState } from 'react';
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
  Radio,
  Button,
  Box,
  CircularProgress,
  Alert,
  TextField,
  Stack,
  MenuItem,
} from '@mui/material';
import { ArrowForward, Close } from '@mui/icons-material';
import { useRecepcion } from '../../context/RecepcionContext';
import { opcionRegistrarBolsin, seleccionarBolsin } from '../../services/bolsinService';

// grdBolsinesEntrantes: mostrarBolsines() / seleccionarBolsin()
const Paso1Bolsines = () => {
  const {
    cmUsuario,
    setCmUsuario,
    bolsines,
    setBolsines,
    detalleBolsin,
    setDetalleBolsin,
    nextStep,
    resetWizard,
    setError,
    loading,
    setLoading,
  } = useRecepcion();

  const [seleccionado, setSeleccionado] = useState(detalleBolsin?.numeroPrecinto ?? null);
  const [localError, setLocalError] = useState('');

  // Paso 4 del CU: selección por listado o filtrando por número de precinto / CM origen
  const [filtroPrecinto, setFiltroPrecinto] = useState('');
  const [filtroCMOrigen, setFiltroCMOrigen] = useState('');

  // Opciones del desplegable: las CM origen de los bolsines entrantes, sin repetir
  const cmsOrigen = [...new Set(bolsines.map((b) => b.cmOrigen))].sort();

  const bolsinesFiltrados = bolsines.filter(
    (b) =>
      String(b.numeroPrecinto).includes(filtroPrecinto.trim()) &&
      (!filtroCMOrigen || b.cmOrigen === filtroCMOrigen)
  );
  const seleccionVisible = bolsinesFiltrados.some((b) => b.numeroPrecinto === seleccionado);

  // A1 / A2: no se encuentra bolsín con el precinto o la CM origen ingresados
  const mensajeSinResultados = () => {
    if (filtroPrecinto.trim() && filtroCMOrigen) {
      return 'No se encuentra bolsín con el número de precinto y la CM de origen ingresados.';
    }
    if (filtroPrecinto.trim()) {
      return 'No se encuentra bolsín con el número de precinto ingresado.';
    }
    return 'No se encuentra bolsín con la CM de origen ingresada.';
  };

  // Observación 1: el EB puede cancelar el caso de uso cuando lo decida
  const handleCancelar = () => {
    setSeleccionado(null);
    setFiltroPrecinto('');
    setFiltroCMOrigen('');
    resetWizard();
  };

  // opcionRegistrarBolsin() -> habilitarPantalla()
  useEffect(() => {
    const habilitarPantalla = async () => {
      setLoading(true);
      const result = await opcionRegistrarBolsin();
      if (result.success) {
        setCmUsuario(result.data.cmUsuario);
        setBolsines(result.data.bolsines);
      } else {
        setError(result.message);
        setLocalError(result.message);
      }
      setLoading(false);
    };

    if (!cmUsuario) {
      habilitarPantalla();
    }
  }, [cmUsuario]);

  const handleNext = async () => {
    setLoading(true);
    setLocalError('');
    const result = await seleccionarBolsin(seleccionado);
    if (result.success) {
      setDetalleBolsin(result.data);
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
          Bolsines entrantes
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Bolsines enviados a <strong>{cmUsuario || '...'}</strong>. Seleccioná el bolsín recibido.
        </Typography>

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress />
          </Box>
        ) : localError ? (
          <Alert severity="error" sx={{ mb: 3 }}>
            {localError}
          </Alert>
        ) : bolsines.length === 0 ? (
          <Alert severity="info">No hay bolsines enviados a tu comisión médica.</Alert>
        ) : (
          <>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 2 }}>
            <TextField
              label="Filtrar por N° de precinto"
              size="small"
              value={filtroPrecinto}
              onChange={(e) => setFiltroPrecinto(e.target.value)}
              inputProps={{ inputMode: 'numeric' }}
              fullWidth
            />
            <TextField
              select
              label="Filtrar por CM origen"
              size="small"
              value={filtroCMOrigen}
              onChange={(e) => setFiltroCMOrigen(e.target.value)}
              fullWidth
            >
              <MenuItem value="">Todas</MenuItem>
              {cmsOrigen.map((cm) => (
                <MenuItem key={cm} value={cm}>
                  {cm}
                </MenuItem>
              ))}
            </TextField>
          </Stack>
          {bolsinesFiltrados.length === 0 ? (
            <Alert severity="warning">{mensajeSinResultados()}</Alert>
          ) : (
          <TableContainer sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 2 }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell padding="checkbox" />
                  <TableCell>N° de precinto</TableCell>
                  <TableCell>CM origen</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {bolsinesFiltrados.map((b) => (
                  <TableRow
                    key={b.numeroPrecinto}
                    hover
                    selected={seleccionado === b.numeroPrecinto}
                    onClick={() => setSeleccionado(b.numeroPrecinto)}
                    sx={{ cursor: 'pointer' }}
                  >
                    <TableCell padding="checkbox">
                      <Radio checked={seleccionado === b.numeroPrecinto} />
                    </TableCell>
                    <TableCell>{b.numeroPrecinto}</TableCell>
                    <TableCell>{b.cmOrigen}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          )}
          </>
        )}

        <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 4 }}>
          <Button variant="outlined" color="secondary" startIcon={<Close />} onClick={handleCancelar} disabled={loading}>
            Cancelar
          </Button>
          <Button
            variant="contained"
            endIcon={<ArrowForward />}
            onClick={handleNext}
            disabled={!seleccionVisible || loading}
            size="large"
          >
            Siguiente
          </Button>
        </Box>
      </CardContent>
    </Card>
  );
};

export default Paso1Bolsines;
