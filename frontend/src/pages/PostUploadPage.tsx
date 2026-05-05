import { FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createPost } from '../api/posts'
import type { PostCategory } from '../types'

const CATEGORIES: PostCategory[] = [
  'COMEDY',
  'VIDEO_GAME',
  'MUSIC',
  'AUTOS_VEHICLES',
  'EDUCATION',
]

export default function PostUploadPage() {
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setError('')
    const form = new FormData(e.currentTarget)
    const videoFile = (e.currentTarget.elements.namedItem('video') as HTMLInputElement).files?.[0]

    if (!videoFile) {
      setError('비디오 파일을 선택해주세요.')
      return
    }

    try {
      setLoading(true)
      await createPost({
        title: form.get('title') as string,
        content: form.get('content') as string,
        category: form.get('category') as string,
        video: videoFile,
      })
      navigate('/')
    } catch {
      setError('업로드에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h1>비디오 업로드</h1>
      <form onSubmit={handleSubmit}>
        <div>
          <label>제목<input name="title" type="text" required /></label>
        </div>
        <div>
          <label>
            내용
            <textarea name="content" required rows={4} />
          </label>
        </div>
        <div>
          <label>
            카테고리
            <select name="category" required defaultValue="">
              <option value="" disabled>선택</option>
              {CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
            </select>
          </label>
        </div>
        <div>
          <label>
            비디오 파일 (mp4)
            <input name="video" type="file" accept="video/mp4,video/*" required />
          </label>
        </div>
        {error && <p role="alert" style={{ color: 'red' }}>{error}</p>}
        <button type="submit" disabled={loading}>
          {loading ? '업로드 중...' : '업로드'}
        </button>
        <button type="button" onClick={() => navigate('/')} style={{ marginLeft: 8 }}>
          취소
        </button>
      </form>
    </div>
  )
}
