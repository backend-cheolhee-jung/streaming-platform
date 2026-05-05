import client from './client'
import type { PageResult, PostDetail, PostItem } from '../types'

export interface GetPostsParams {
  keyword?: string
  category?: string
  page?: number
  size?: number
}

export interface CreatePostPayload {
  title: string
  content: string
  category: string
  video: File
}

export const getPosts = (params: GetPostsParams = {}) =>
  client.get<PageResult<PostItem>>('/streams/posts', { params })

export const getPost = (postId: number) =>
  client.get<PostDetail>(`/streams/posts/${postId}`)

export const createPost = ({ title, content, category, video }: CreatePostPayload) => {
  const form = new FormData()
  form.append('video', video, video.name)
  form.append('title', title)
  form.append('content', content)
  form.append('category', category)
  return client.post('/streams/posts', form)
}

export const getVideoStream = (postId: number, videoId: number, token: string) =>
  fetch(`/streams/posts/${postId}/videos/${videoId}`, {
    headers: { Authorization: `Bearer ${token}` },
  })
