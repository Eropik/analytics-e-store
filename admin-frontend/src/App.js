import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import UserManagement from './pages/UserManagement';
import ProductManagement from './pages/ProductManagement';
import OrderManagement from './pages/OrderManagement';
import Analytics from './pages/Analytics';
import Login from './pages/Login';
import CityManagement from './pages/CityManagement';
import './App.css';

function App() {
  return (
    <Router>
      <div className="App">
        <Navbar />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Analytics />} />
            <Route path="/login" element={<Login />} />
            <Route path="/users" element={<UserManagement />} />
            <Route path="/products" element={<ProductManagement />} />
            <Route path="/orders" element={<OrderManagement />} />
            <Route path="/analytics" element={<Analytics />} />
            <Route path="/cities" element={<CityManagement />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;



