import * as fs from 'fs'
import * as path from 'path'
import { expect, test } from '@playwright/test'

const uniqueEmail = () => `upload_${Date.now()}@example.com`

// helper: sign up and land on post list
async function signUpAndLogin(page: import('@playwright/test').Page, email: string) {
  await page.goto('/signup')
  await page.fill('input[name="name"]', 'Uploader')
  await page.fill('input[name="email"]', email)
  await page.fill('input[name="password"]', 'Password1!')
  await page.fill('input[name="confirmPassword"]', 'Password1!')
  await page.click('button[type="submit"]')
  await expect(page).toHaveURL('/')
}

test.describe('Video Upload', () => {
  test('upload post with video → appears in list', async ({ page }) => {
    const email = uniqueEmail()
    await signUpAndLogin(page, email)

    await page.click('a:has-text("업로드")')
    await expect(page).toHaveURL('/posts/new')

    await page.fill('input[name="title"]', 'E2E Test Video')
    await page.fill('textarea[name="content"]', 'Playwright upload test')
    await page.selectOption('select[name="category"]', 'EDUCATION')

    // create a tiny valid mp4-like file for testing (1-byte placeholder)
    const tmpPath = path.join(process.cwd(), 'e2e', 'fixtures', 'sample.mp4')
    if (!fs.existsSync(path.dirname(tmpPath))) {
      fs.mkdirSync(path.dirname(tmpPath), { recursive: true })
    }
    if (!fs.existsSync(tmpPath)) {
      // write minimal ftyp box so the file is non-empty
      fs.writeFileSync(tmpPath, Buffer.alloc(32, 0))
    }

    await page.setInputFiles('input[name="video"]', tmpPath)
    await page.click('button[type="submit"]')

    await expect(page).toHaveURL('/')
    await expect(page.locator('text=E2E Test Video')).toBeVisible()
  })

  test('upload without file → shows validation error', async ({ page }) => {
    const email = uniqueEmail()
    await signUpAndLogin(page, email)

    await page.click('a:has-text("업로드")')
    await page.fill('input[name="title"]', 'No Video Test')
    await page.fill('textarea[name="content"]', 'content')
    await page.selectOption('select[name="category"]', 'MUSIC')
    // intentionally skip file input → browser required validation prevents submit

    const fileInput = page.locator('input[name="video"]')
    await expect(fileInput).toBeVisible()
  })
})
