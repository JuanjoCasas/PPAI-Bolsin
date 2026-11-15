import {
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Paper,
  Typography,
  Box,
} from '@mui/material';
import { CheckCircle, RadioButtonUnchecked, Inbox, FactCheck, Flag } from '@mui/icons-material';
import { useRecepcion, TOTAL_PASOS } from '../../context/RecepcionContext';

const steps = [
  { id: 1, label: 'Bolsines entrantes', icon: Inbox },
  { id: 2, label: 'Detalle y recepción', icon: FactCheck },
  { id: 3, label: 'Resultado', icon: Flag },
];

const Sidebar = () => {
  const { currentStep, completedSteps, goToStep, cmUsuario } = useRecepcion();

  const isStepCompleted = (stepId) => completedSteps.includes(stepId);
  const isStepAccessible = (stepId) =>
    currentStep !== TOTAL_PASOS && (stepId === 1 || completedSteps.includes(stepId - 1));

  return (
    <Paper
      elevation={0}
      sx={{
        height: '100%',
        borderRadius: 0,
        borderRight: '1px solid',
        borderColor: 'divider',
        bgcolor: 'background.paper',
      }}
    >
      <Box sx={{ p: 3, borderBottom: '1px solid', borderColor: 'divider' }}>
        <Typography variant="h6" color="primary" fontWeight="bold">
          CU 28
        </Typography>
        {/* lblCMUsuario: mostrarCMUsuario() */}
        <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
          {cmUsuario || 'Cargando...'}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Paso {currentStep} de {TOTAL_PASOS}
        </Typography>
      </Box>

      <List sx={{ p: 2 }}>
        {steps.map((step) => {
          const Icon = step.icon;
          const completed = isStepCompleted(step.id);
          const current = currentStep === step.id;
          const accessible = current || isStepAccessible(step.id);

          return (
            <ListItem key={step.id} disablePadding sx={{ mb: 1 }}>
              <ListItemButton
                onClick={() => accessible && goToStep(step.id)}
                disabled={!accessible}
                selected={current}
                sx={{
                  borderRadius: 2,
                  '&.Mui-selected': {
                    bgcolor: 'primary.light',
                    color: 'primary.contrastText',
                    '&:hover': { bgcolor: 'primary.main' },
                  },
                  '&.Mui-disabled': { opacity: 0.5 },
                }}
              >
                <ListItemIcon sx={{ minWidth: 40, color: 'inherit' }}>
                  {completed ? (
                    <CheckCircle color={current ? 'inherit' : 'success'} />
                  ) : current ? (
                    <Icon color="inherit" />
                  ) : (
                    <RadioButtonUnchecked color={accessible ? 'inherit' : 'disabled'} />
                  )}
                </ListItemIcon>
                <ListItemText
                  primary={step.label}
                  primaryTypographyProps={{ fontWeight: current ? 600 : 400, fontSize: '0.95rem' }}
                />
              </ListItemButton>
            </ListItem>
          );
        })}
      </List>
    </Paper>
  );
};

export default Sidebar;
