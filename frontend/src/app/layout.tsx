import type { Metadata } from 'next';
import QueryProvider from '@/components/providers/QueryProvider';
import ThemeScript from '@/components/providers/ThemeScript';
import './globals.css';

export const metadata: Metadata = {
  title: 'ContractPulse',
  description: 'SaaS de Gestão de Contratos Transparentes',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="pt-BR" suppressHydrationWarning>
      <head>
        <ThemeScript />
      </head>
      <body className="bg-gray-50 text-gray-900 antialiased transition-colors duration-300 dark:bg-dark-bg dark:text-gray-100">
        <QueryProvider>{children}</QueryProvider>
      </body>
    </html>
  );
}
