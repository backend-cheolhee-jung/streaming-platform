import axios from 'axios'

const TOKEN_KEY = 'access_token'

export const getToken = () => localStorage.getItem(TOKEN_KEY)
export const setToken = (token: string) => localStorage.setItem(TOKEN_KEY, token)
export const clearToken = () => localStorage.removeItem(TOKEN_KEY)

const client = axios.create({
  baseURL: '',
  withCredentials: true,
})

client.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use((response) => {
  const auth = response.headers['authorization']
  if (auth) {
    setToken(auth.replace(/^Bearer\s+/i, ''))
  }
  return response
})

export default client
