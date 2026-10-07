import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import api from "./client";
import type { Appointment, Doctor, DoctorForm, Page, Patient, PatientForm } from "./types";

export interface PatientQuery {
  page: number;
  name: string;
  email: string;
}

export function usePatients(q: PatientQuery) {
  return useQuery({
    queryKey: ["patients", q],
    queryFn: () =>
      api
        .get<Page<Patient>>("/patients", {
          params: { page: q.page, size: 10, sort: "name", ...(q.name && { name: q.name }), ...(q.email && { email: q.email }) },
        })
        .then((r) => r.data),
  });
}

export function useCreatePatient() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: PatientForm) => api.post<Patient>("/patients", body).then((r) => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["patients"] }),
  });
}

export function useUpdatePatient(id: string | null) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: PatientForm) => api.put<Patient>(`/patients/${id}`, body).then((r) => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["patients"] }),
  });
}

export function useDeletePatient() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => api.delete(`/patients/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["patients"] }),
  });
}

export interface DoctorQuery {
  page: number;
  name: string;
  specialty: string;
}

export function useDoctors(q: DoctorQuery) {
  return useQuery({
    queryKey: ["doctors", q],
    queryFn: () =>
      api
        .get<Page<Doctor>>("/doctors", {
          params: { page: q.page, size: 10, sort: "name", ...(q.name && { name: q.name }), ...(q.specialty && { specialty: q.specialty }) },
        })
        .then((r) => r.data),
  });
}

export function useCreateDoctor() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: DoctorForm) => api.post<Doctor>("/doctors", body).then((r) => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["doctors"] }),
  });
}

export function useUpdateDoctor(id: number | null) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: DoctorForm) => api.put<Doctor>(`/doctors/${id}`, body).then((r) => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["doctors"] }),
  });
}

export function useDeleteDoctor() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.delete(`/doctors/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["doctors"] }),
  });
}

export interface AppointmentQuery {
  page: number;
  doctorId: string;
  patientId: string;
  from: string;
  to: string;
}

export function useAppointments(q: AppointmentQuery) {
  return useQuery({
    queryKey: ["appointments", q],
    queryFn: () =>
      api
        .get<Page<Appointment>>("/appointments", {
          params: {
            page: q.page,
            size: 10,
            sort: "appointmentDate",
            ...(q.doctorId && { doctorId: Number(q.doctorId) }),
            ...(q.patientId && { patientId: q.patientId }),
            ...(q.from && { from: q.from }),
            ...(q.to && { to: q.to }),
          },
        })
        .then((r) => r.data),
  });
}

export function useCreateAppointment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: { doctorId: number; patientId: string; appointmentDate: string; notes?: string }) =>
      api.post<Appointment>("/appointments", body).then((r) => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["appointments"] }),
  });
}

export function useDeleteAppointment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.delete(`/appointments/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["appointments"] }),
  });
}

export function useDoctorOptions() {
  return useQuery({
    queryKey: ["doctor-options"],
    queryFn: () => api.get<Page<Doctor>>("/doctors", { params: { page: 0, size: 100, sort: "name" } }).then((r) => r.data.content),
  });
}

export function usePatientOptions(name: string) {
  return useQuery({
    queryKey: ["patient-options", name],
    queryFn: () =>
      api
        .get<Page<Patient>>("/patients", { params: { page: 0, size: 20, sort: "name", ...(name && { name }) } })
        .then((r) => r.data.content),
  });
}
