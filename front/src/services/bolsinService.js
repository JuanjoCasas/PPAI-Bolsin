import axios from 'axios';

// Cada función corresponde a un mensaje del actor (EB) a la boundary.
const API_URL = '/bolsines';

const handleError = (error) => {
  if (error.response) {
    return {
      success: false,
      message: error.response.data?.error || error.response.data?.mensaje || 'Error en la solicitud',
      status: error.response.status,
    };
  }
  if (error.request) {
    return {
      success: false,
      message: 'No se pudo conectar con el servidor. Asegurate de que el backend esté ejecutándose.',
      status: 0,
    };
  }
  return { success: false, message: error.message || 'Error desconocido', status: 0 };
};

const post = async (path, body) => {
  try {
    const response = await axios.post(`${API_URL}${path}`, body);
    return { success: true, data: response.data };
  } catch (error) {
    return handleError(error);
  }
};

// opcionRegistrarBolsin() -> mostrarCMUsuario() + mostrarBolsines()
export const opcionRegistrarBolsin = () => post('/iniciar');

// seleccionarBolsin() -> mostrarDetalleBolsin() + mostrarOpcionesRecepcionBolsin()
export const seleccionarBolsin = (numeroPrecinto) => post('/seleccionar-bolsin', { numeroPrecinto });

// seleccionaOpcionContenidoCoincide() -> pedirConfirmacion()
export const seleccionarOpcion = (opcion) => post('/seleccionar-opcion', { opcion });

// confirmar() -> informarEjecucionExitosa()
export const confirmar = () => post('/confirmar');
