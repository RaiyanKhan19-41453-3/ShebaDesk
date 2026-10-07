export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface Patient {
  id: string;
  name: string;
  location: string;
  email: string;
  dateOfBirth: string;
  registeredDate: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface PatientForm {
  name: string;
  email: string;
  location: string;
  dateOfBirth: string;
  registeredDate?: string;
}

export interface Doctor {
  id: number;
  name: string;
  specialty: string;
  email: string;
  phone?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface DoctorForm {
  name: string;
  specialty: string;
  email: string;
  phone?: string;
}

export interface Appointment {
  id: number;
  doctorId: number;
  doctorName: string;
  patientId: string;
  patientName: string;
  appointmentDate: string;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface AppointmentForm {
  doctorId: number | "";
  patientId: string;
  appointmentDate: string;
  notes?: string;
}
