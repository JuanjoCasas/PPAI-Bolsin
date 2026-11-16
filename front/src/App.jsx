import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { RecepcionProvider } from './context/RecepcionContext';
import MainLayout from './components/layout/MainLayout';
import theme from './theme';

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <RecepcionProvider>
        <MainLayout />
      </RecepcionProvider>
    </ThemeProvider>
  );
}

export default App;
