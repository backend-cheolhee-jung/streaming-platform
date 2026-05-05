import * as fs from 'fs'
import * as path from 'path'
import { expect, test } from '@playwright/test'

const uniqueEmail = () => `watch_${Date.now()}@example.com`

async function signUpAndLogin(page: import('@playwright/test').Page, email: string) {
  await page.goto('/signup')
  await page.fill('input[name="name"]', 'Watcher')
  await page.fill('input[name="email"]', email)
  await page.fill('input[name="password"]', 'Password1!')
  await page.fill('input[name="confirmPassword"]', 'Password1!')
  await page.click('button[type="submit"]')
  await expect(page).toHaveURL('/')
}

async function uploadPost(page: import('@playwright/test').Page, title: string) {
  await page.click('a:has-text("업로드")')
  await page.fill('input[name="title"]', title)
  await page.fill('textarea[name="content"]', 'watch test content')
  await page.selectOption('select[name="category"]', 'COMEDY')

  const tmpPath = path.join(process.cwd(), 'e2e', 'fixtures', 'sample.mp4')
  if (!fs.existsSync(path.dirname(tmpPath))) {
    fs.mkdirSync(path.dirname(tmpPath), { recursive: true })
  }
  if (!fs.existsSync(tmpPath)) {
    fs.writeFileSync(tmpPath, Buffer.alloc(32, 0))
  }

  await page.setInputFiles('input[name="video"]', tmpPath)
  await page.click('button[type="submit"]')
  await expect(page).toHaveURL('/')
}

test.describe('Video Watch', () => {
  test('click post → video watch page contains video element', async ({ page }) => {
    const email = uniqueEmail()
    await signUpAndLogin(page, email)

    const title = `Watch Test ${Date.now()}`
    await uploadPost(page, title)

    await page.click(`text=${title}`)
    await expect(page).toHaveURL(/\/posts\/\d+/)
    await expect(page.locator('h1')).toContainText(title)
    await expect(page.locator('video')).toBeVisible()
  })

  test('back button returns to post list', async ({ page }) => {
    const email = uniqueEmail()
    await signUpAndLogin(page, email)

    const title = `Back Test ${Date.now()}`
    await uploadPost(page, title)

    await page.click(`text=${title}`)
    await expect(page).toHaveURL(/\/posts\/\d+/)

    await page.click('button:has-text("목록으로")')
    await expect(page).toHaveURL('/')
  })
})
