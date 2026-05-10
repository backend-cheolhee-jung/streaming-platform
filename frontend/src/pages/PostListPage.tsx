import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getPosts } from '../api/posts'
import { logout } from '../api/auth'
import type { PostCategory, PostItem } from '../types'

const CATEGORIES: PostCategory[] = [
  'COMEDY',
  'VIDEO_GAME',
  'MUSIC',
  'AUTOS_VEHICLES',
  'EDUCATION',
]

export default function PostListPage() {
  const navigate = useNavigate()
  const [posts, setPosts] = useState<PostItem[]>([])
  const [keyword, setKeyword] = useState('')
  const [category, setCategory] = useState('')
  const [loading, setLoading] = useState(false)

  const fetchPosts = async () => {
    setLoading(true)
    try {
      const res = await getPosts({ keyword: keyword || undefined, category: category || undefined, page: 0, size: 20 })
      const data = res.data
      setPosts(Array.isArray(data) ? data : data.content ?? [])
    } catch {
      setPosts([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchPosts() }, [])

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
        <h1>비디오 목록</h1>
        <div>
          <Link to="/posts/new"><button>업로드</button></Link>
          <button onClick={handleLogout} style={{ marginLeft: 8 }}>로그아웃</button>
        </div>
      </div>

      <div style={{ marginBottom: 16 }}>
        <input
          placeholder="검색어"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
        <select value={category} onChange={(e) => setCategory(e.target.value)}>
          <option value="">전체 카테고리</option>
          {CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
        </select>
        <button onClick={fetchPosts}>검색</button>
      </div>

      {loading && <p>불러오는 중...</p>}

      {!loading && posts.length === 0 && <p>게시물이 없습니다.</p>}

      <ul style={{ listStyle: 'none', padding: 0 }}>
        {posts.map((post) => (
          <li key={post.id} style={{ borderBottom: '1px solid #ccc', padding: '8px 0' }}>
            <Link to={`/posts/${post.id}`}>
              [{post.category}] {post.title}
            </Link>
            <span style={{ marginLeft: 8, color: '#888' }}>작성자 #{post.author}</span>
          </li>
        ))}
      </ul>
    </div>
  )
}
