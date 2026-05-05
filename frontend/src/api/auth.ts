import client, { clearToken } from './client'

export interface SignUpPayload {
  email: string
  password: string
  confirmPassword: string
  name: string
}

export interface LoginPayload {
  email: string
  password: string
}

export const signUp = (payload: SignUpPayload) =>
  client.post('/users/signup', payload)

export const login = (payload: LoginPayload) =>
  client.post('/users/login', payload)

export const logout = () =>
  client.delete('/users/logout').finally(() => clearToken())
