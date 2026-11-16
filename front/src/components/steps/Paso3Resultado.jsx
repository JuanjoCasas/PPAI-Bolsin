import { Card, CardContent, Typography, Button, Box, Alert, List, ListItem, ListItemIcon, ListItemText } from '@mui/material';
import { CheckCircle, Refresh, MailOutline } from '@mui/icons-material';
import { useRecepcion } from '../../context/RecepcionContext';

// modalEjecucionExitosa: informarEjecucionExitosa()
const Paso3Resultado = () => {
  const { mensajeResultado, notificaciones, detalleBolsin, resetWizard } = useRecepcion();

  return (
    <Card>
      <CardContent sx={{ p: 4, textAlign: 'center' }}>
        <CheckCircle sx={{ fontSize: 100, color: 'success.main', mb: 2 }} />
        <Typography variant="h4" gutterBottom color="success.main" fontWeight="bold">
          ¡Recepción registrada!
        </Typography>
        <Typography variant="body1" color="text.secondary">
          {mensajeResultado}
        </Typography>
        {detalleBolsin && (
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            Bolsín precinto N° {detalleBolsin.numeroPrecinto} ({detalleBolsin.cmOrigen})
          </Typography>
        )}

        {/* <<include>> CU 29 Notificar recepción de bolsín */}
        {notificaciones.length > 0 && (
          <Alert
            severity={notificaciones.some((n) => n.resultadoEnvio.startsWith('Error')) ? 'warning' : 'info'}
            icon={<MailOutline />}
            sx={{ mt: 3, textAlign: 'left' }}
          >
            Notificación por email de la recepción (CU 29):
            <List dense disablePadding>
              {notificaciones.map((n) => (
                <ListItem key={n.email} disableGutters>
                  <ListItemIcon sx={{ minWidth: 16 }}>•</ListItemIcon>
                  <ListItemText
                    primary={`${n.cmOrigen} (${n.email})`}
                    secondary={`${n.cantidadDocumentaciones} documentación(es) informada(s) · ${n.resultadoEnvio}`}
                  />
                </ListItem>
              ))}
            </List>
          </Alert>
        )}

        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
          <Button variant="contained" size="large" startIcon={<Refresh />} onClick={resetWizard} sx={{ px: 4 }}>
            Volver al menú principal
          </Button>
        </Box>
      </CardContent>
    </Card>
  );
};

export default Paso3Resultado;
