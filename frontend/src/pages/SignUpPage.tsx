import { FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { signUp } from '../api/auth'

export default function SignUpPage() {
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setError('')
    const form = new FormData(e.currentTarget)
    const payload = {
      email: form.get('email') as string,
      password: form.get('password') as string,
      confirmPassword: form.get('confirmPassword') as string,
      name: form.get('name') as string,
    }

    try {
      setLoading(true)
      await signUp(payload)
      navigate('/')
    } catch {
      setError('회원가입에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h1>회원가입</h1>
      <form onSubmit={handleSubmit}>
        <div>
          <label>이름<input name="name" type="text" required /></label>
        </div>
        <div>
          <label>이메일<input name="email" type="email" required /></label>
        </div>
        <div>
          <label>비밀번호<input name="password" type="password" required /></label>
        </div>
        <div>
          <label>비밀번호 확인<input name="confirmPassword" type="password" required /></label>
        </div>
        {error && <p role="alert" style={{ color: 'red' }}>{error}</p>}
        <button type="submit" disabled={loading}>
          {loading ? '처리 중...' : '가입하기'}
        </button>
      </form>
      <p>이미 계정이 있으신가요? <Link to="/login">로그인</Link></p>
    </div>
  )
}
