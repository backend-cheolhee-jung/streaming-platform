import { BrowserRouter, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import LoginPage from './pages/LoginPage'
import PostListPage from './pages/PostListPage'
import PostUploadPage from './pages/PostUploadPage'
import SignUpPage from './pages/SignUpPage'
import VideoWatchPage from './pages/VideoWatchPage'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/signup" element={<SignUpPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route element={<ProtectedRoute />}>
          <Route path="/" element={<PostListPage />} />
          <Route path="/posts/new" element={<PostUploadPage />} />
          <Route path="/posts/:postId" element={<VideoWatchPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
