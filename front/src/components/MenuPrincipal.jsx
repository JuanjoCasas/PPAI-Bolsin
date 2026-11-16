import { useState } from 'react';
import { Card, CardActionArea, CardContent, Typography, Box, Alert, CircularProgress } from '@mui/material';
import { MoveToInbox } from '@mui/icons-material';
import { useRecepcion } from '../context/RecepcionContext';
import { opcionRegistrarBolsin } from '../services/bolsinService';

// Paso 1 del CU: el EB selecciona la opción para registrar la recepción de un bolsín.
// EB -> Boundary : opcionRegistrarBolsin()
const MenuPrincipal = () => {
  const { setCuIniciado, setCmUsuario, setBolsines, setError, loading, setLoading } = useRecepcion();
  const [localError, setLocalError] = useState('');

  // Boundary -> Boundary : habilitarPantalla()
  // Boundary -> Control : opcionRegistrarBolsin() -> mostrarCMUsuario() + mostrarBolsines()
  const habilitarPantalla = async () => {
    setLoading(true);
    setLocalError('');
    const result = await opcionRegistrarBolsin();
    if (result.success) {
      setCmUsuario(result.data.cmUsuario);
      setBolsines(result.data.bolsines);
      setCuIniciado(true);
    } else {
      setError(result.message);
      setLocalError(result.message);
    }
    setLoading(false);
  };

  return (
    <Box sx={{ maxWidth: 720, mx: 'auto' }}>
      <Typography variant="h5" color="primary" fontWeight="bold" gutterBottom>
        Gestión de bolsines
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Seleccioná la operación que querés realizar.
      </Typography>

      <Card variant="outlined">
        <CardActionArea onClick={habilitarPantalla} disabled={loading} sx={{ p: 1 }}>
          <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            {loading ? <CircularProgress size={40} /> : <MoveToInbox color="primary" sx={{ fontSize: 40 }} />}
            <Box>
              <Typography variant="h6" fontWeight="bold">
                Registrar recepción de bolsín
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Registrá la llegada de un bolsín a tu comisión médica y la situación de su documentación.
              </Typography>
            </Box>
          </CardContent>
        </CardActionArea>
      </Card>

      {localError && (
        <Alert severity="error" sx={{ mt: 3 }}>
          {localError}
        </Alert>
      )}
    </Box>
  );
};

export default MenuPrincipal;
