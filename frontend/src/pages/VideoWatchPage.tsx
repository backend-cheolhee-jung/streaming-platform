import { useEffect, useRef, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { getPost, getVideoStream } from '../api/posts'
import { getToken } from '../api/client'
import type { PostDetail } from '../types'

export default function VideoWatchPage() {
  const { postId } = useParams<{ postId: string }>()
  const navigate = useNavigate()
  const videoRef = useRef<HTMLVideoElement>(null)
  const [post, setPost] = useState<PostDetail | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!postId) return

    const id = Number(postId)
    getPost(id)
      .then((res) => setPost(res.data))
      .catch(() => setError('게시물을 불러올 수 없습니다.'))
      .finally(() => setLoading(false))
  }, [postId])

  useEffect(() => {
    if (!post) return

    // The backend stores one video per post; video id equals post id
    const token = getToken()
    if (!token) return

    const videoId = post.id

    getVideoStream(post.id, videoId, token)
      .then(async (response) => {
        if (!response.ok) {
          setError('비디오를 불러올 수 없습니다.')
          return
        }
        const blob = await response.blob()
        const url = URL.createObjectURL(blob)
        if (videoRef.current) {
          videoRef.current.src = url
        }
      })
      .catch(() => setError('비디오 스트리밍 오류가 발생했습니다.'))
  }, [post])

  if (loading) return <p>불러오는 중...</p>
  if (error) return <div><p style={{ color: 'red' }}>{error}</p><button onClick={() => navigate('/')}>목록으로</button></div>

  return (
    <div>
      <button onClick={() => navigate('/')}>← 목록으로</button>
      {post && (
        <>
          <h1>{post.title}</h1>
          <p style={{ color: '#888' }}>[{post.category}] 작성자 #{post.author}</p>
          <p>{post.content}</p>
          <video
            ref={videoRef}
            controls
            style={{ width: '100%', maxWidth: 800, display: 'block', marginTop: 16 }}
          />
        </>
      )}
    </div>
  )
}
