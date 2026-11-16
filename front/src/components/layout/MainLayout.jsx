import { Box, Container, Grid, AppBar, Toolbar, Typography, Snackbar, Alert } from '@mui/material';
import { Inventory2 } from '@mui/icons-material';
import Sidebar from './Sidebar';
import { useRecepcion } from '../../context/RecepcionContext';

import Paso1Bolsines from '../steps/Paso1Bolsines';
import Paso2Detalle from '../steps/Paso2Detalle';
import Paso3Resultado from '../steps/Paso3Resultado';

// BoundaryRegistrarRecepcionBolsin (parte visual)
const MainLayout = () => {
  const { currentStep, error, setError } = useRecepcion();

  const renderStep = () => {
    switch (currentStep) {
      case 2:
        return <Paso2Detalle />;
      case 3:
        return <Paso3Resultado />;
      default:
        return <Paso1Bolsines />;
    }
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <AppBar position="static" elevation={1}>
        <Toolbar>
          <Inventory2 sx={{ mr: 2 }} />
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            Registrar recepción de bolsín
          </Typography>
        </Toolbar>
      </AppBar>

      <Box sx={{ flexGrow: 1, display: 'flex' }}>
        <Grid container sx={{ height: '100%' }}>
          <Grid item xs={12} md={3}>
            <Sidebar />
          </Grid>
          <Grid item xs={12} md={9}>
            <Container maxWidth="lg" sx={{ py: 4 }}>
              {renderStep()}
            </Container>
          </Grid>
        </Grid>
      </Box>

      <Snackbar
        open={!!error}
        autoHideDuration={6000}
        onClose={() => setError(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert onClose={() => setError(null)} severity="error" sx={{ width: '100%' }}>
          {error}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default MainLayout;
