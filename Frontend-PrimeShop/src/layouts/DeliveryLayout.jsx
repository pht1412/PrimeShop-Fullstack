import React from 'react';
import { Outlet } from 'react-router-dom';

const DeliveryLayout = () => {
  return (
    <div style={{ display: 'flex' }}>
      {/* Simple Sidebar */}
      <div style={{ 
        width: '250px', 
        backgroundColor: '#f5f5f5', 
        padding: '20px',
        minHeight: '100vh'
      }}>
        <h2>🚚 PrimeShop Delivery</h2>
        <p>Delivery Management System</p>
        <hr />
        <ul style={{ listStyle: 'none', padding: 0 }}>
          <li style={{ padding: '10px 0' }}>
            <a href="/delivery-dashboard" style={{ textDecoration: 'none', color: '#1976d2' }}>
              📊 Dashboard
            </a>
          </li>
          <li style={{ padding: '10px 0' }}>
            <a href="/delivery-dashboard/orders" style={{ textDecoration: 'none', color: '#666' }}>
              📦 Orders
            </a>
          </li>
          <li style={{ padding: '10px 0' }}>
            <a href="/" style={{ textDecoration: 'none', color: '#666' }}>
              🏠 Home
            </a>
          </li>
        </ul>
      </div>

      {/* Main Content */}
      <div style={{ flex: 1, padding: '20px' }}>
        <Outlet />
      </div>
    </div>
  );
};

export default DeliveryLayout;
