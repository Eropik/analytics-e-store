import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import ProtectedAdminRoute from './components/ProtectedAdminRoute';
import UserManagement from './pages/UserManagement';
import ProductManagement from './pages/ProductManagement';
import OrderManagement from './pages/OrderManagement';
import Analytics from './pages/Analytics';
import Login from './pages/Login';
import CityManagement from './pages/CityManagement';
import ToastHost from './components/ToastHost';
import './App.css';

function App() {
  return (
    <Router>
      <div className="App">
        <ToastHost />
        <Navbar />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Analytics />} />
            <Route path="/login" element={<Login />} />
            <Route
              path="/users"
              element={
                <ProtectedAdminRoute permissionKeys={['hasUserManagement']}>
                  <UserManagement />
                </ProtectedAdminRoute>
              }
            />
            <Route
              path="/products"
              element={
                <ProtectedAdminRoute permissionKeys={['hasProductManagement']}>
                  <ProductManagement />
                </ProtectedAdminRoute>
              }
            />
            <Route
              path="/orders"
              element={
                <ProtectedAdminRoute permissionKeys={['hasOrderManagement']}>
                  <OrderManagement />
                </ProtectedAdminRoute>
              }
            />
            <Route path="/analytics" element={<Analytics />} />
            <Route
              path="/cities"
              element={
                <ProtectedAdminRoute permissionKeys={['hasProductManagement', 'hasOrderManagement']}>
                  <CityManagement />
                </ProtectedAdminRoute>
              }
            />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;



