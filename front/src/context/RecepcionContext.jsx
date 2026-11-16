import { createContext, useContext, useState } from 'react';

const RecepcionContext = createContext();

export const TOTAL_PASOS = 3;

export const useRecepcion = () => {
  const context = useContext(RecepcionContext);
  if (!context) {
    throw new Error('useRecepcion debe ser usado dentro de RecepcionProvider');
  }
  return context;
};

export const RecepcionProvider = ({ children }) => {
  const [currentStep, setCurrentStep] = useState(1);
  const [completedSteps, setCompletedSteps] = useState([]);

  const [cmUsuario, setCmUsuario] = useState('');
  const [bolsines, setBolsines] = useState([]);
  const [detalleBolsin, setDetalleBolsin] = useState(null);
  const [mensajeResultado, setMensajeResultado] = useState('');
  const [notificaciones, setNotificaciones] = useState([]); // CU 29

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const nextStep = () => {
    if (currentStep < TOTAL_PASOS) {
      if (!completedSteps.includes(currentStep)) {
        setCompletedSteps([...completedSteps, currentStep]);
      }
      setCurrentStep(currentStep + 1);
    }
  };

  const prevStep = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  const goToStep = (step) => {
    // Una vez confirmada la recepción no se puede volver atrás
    if (currentStep === TOTAL_PASOS) return;
    if (step <= 1 || completedSteps.includes(step - 1)) {
      setCurrentStep(step);
    }
  };

  const resetWizard = () => {
    setCurrentStep(1);
    setCompletedSteps([]);
    setCmUsuario('');
    setBolsines([]);
    setDetalleBolsin(null);
    setMensajeResultado('');
    setNotificaciones([]);
    setError(null);
  };

  const value = {
    currentStep,
    completedSteps,
    cmUsuario,
    setCmUsuario,
    bolsines,
    setBolsines,
    detalleBolsin,
    setDetalleBolsin,
    mensajeResultado,
    setMensajeResultado,
    notificaciones,
    setNotificaciones,
    loading,
    setLoading,
    error,
    setError,
    nextStep,
    prevStep,
    goToStep,
    resetWizard,
  };

  return <RecepcionContext.Provider value={value}>{children}</RecepcionContext.Provider>;
};
