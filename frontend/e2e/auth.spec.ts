import { expect, test } from '@playwright/test'

const uniqueEmail = () => `test_${Date.now()}@example.com`

test.describe('Auth', () => {
  test('signup → redirects to post list', async ({ page }) => {
    await page.goto('/signup')
    await page.fill('input[name="name"]', 'Test User')
    await page.fill('input[name="email"]', uniqueEmail())
    await page.fill('input[name="password"]', 'Password1!')
    await page.fill('input[name="confirmPassword"]', 'Password1!')
    await page.click('button[type="submit"]')

    await expect(page).toHaveURL('/')
    await expect(page.getByRole('heading', { name: '비디오 목록' })).toBeVisible()
  })

  test('login with valid credentials → redirects to post list', async ({ page }) => {
    const email = uniqueEmail()
    // sign up first
    await page.goto('/signup')
    await page.fill('input[name="name"]', 'Login Test')
    await page.fill('input[name="email"]', email)
    await page.fill('input[name="password"]', 'Password1!')
    await page.fill('input[name="confirmPassword"]', 'Password1!')
    await page.click('button[type="submit"]')
    await expect(page).toHaveURL('/')

    // clear token and login again
    await page.evaluate(() => localStorage.clear())
    await page.goto('/login')
    await page.fill('input[name="email"]', email)
    await page.fill('input[name="password"]', 'Password1!')
    await page.click('button[type="submit"]')

    await expect(page).toHaveURL('/')
    await expect(page.getByRole('heading', { name: '비디오 목록' })).toBeVisible()
  })

  test('login with wrong password → shows error', async ({ page }) => {
    await page.goto('/login')
    await page.fill('input[name="email"]', 'wrong@example.com')
    await page.fill('input[name="password"]', 'wrongpass')
    await page.click('button[type="submit"]')

    await expect(page.getByRole('alert')).toBeVisible()
    await expect(page).toHaveURL('/login')
  })

  test('unauthenticated user is redirected to login', async ({ page }) => {
    await page.evaluate(() => localStorage.clear())
    await page.goto('/')
    await expect(page).toHaveURL('/login')
  })

  test('logout clears session and redirects to login', async ({ page }) => {
    const email = uniqueEmail()
    await page.goto('/signup')
    await page.fill('input[name="name"]', 'Logout Test')
    await page.fill('input[name="email"]', email)
    await page.fill('input[name="password"]', 'Password1!')
    await page.fill('input[name="confirmPassword"]', 'Password1!')
    await page.click('button[type="submit"]')
    await expect(page).toHaveURL('/')

    await page.click('button:has-text("로그아웃")')
    await expect(page).toHaveURL('/login')
  })
})
