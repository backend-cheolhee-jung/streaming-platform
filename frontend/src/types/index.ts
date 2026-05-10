export type PostCategory =
  | 'COMEDY'
  | 'VIDEO_GAME'
  | 'MUSIC'
  | 'AUTOS_VEHICLES'
  | 'EDUCATION'

export interface PostItem {
  id: number
  title: string
  author: number
  category: PostCategory
}

export interface PostDetail {
  id: number
  title: string
  content: string
  author: number
  category: PostCategory
}

export interface VideoDetail {
  id: number
  name: string
  uniqueName: string
  size: number
  extension: string
  owner: number
  post: number
}

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}
