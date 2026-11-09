export interface User {
    id: string;
    avatar: string;
    username: string;
    fullName: string;
    email: string;
    phoneNumber: string;
    address: string;
    roles?: string[];
}
