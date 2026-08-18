export interface ImportTransaction {
    id: number;
    productId: number;
    quantity: number;
    unitCost: number;
    supplierName: string;
    invoiceNumber: number;
    notes: string;
    importDate: string;
}