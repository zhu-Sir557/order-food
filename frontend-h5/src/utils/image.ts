/**
 * 客户端图片压缩/缩图（头像上传前调用）
 * @param file 原图文件
 * @param maxSize 最长边像素上限（默认 512）
 * @param quality JPEG 压缩质量（默认 0.85）
 * @returns 压缩后的 Blob（JPEG）
 */
export function compressImage(file: File, maxSize = 512, quality = 0.85): Promise<Blob> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onerror = () => reject(new Error('读取图片失败'))
    reader.onload = () => {
      const img = new Image()
      img.onerror = () => reject(new Error('解析图片失败'))
      img.onload = () => {
        const scale = Math.min(1, maxSize / Math.max(img.width, img.height))
        const w = Math.round(img.width * scale)
        const h = Math.round(img.height * scale)
        const canvas = document.createElement('canvas')
        canvas.width = w
        canvas.height = h
        const ctx = canvas.getContext('2d')
        if (!ctx) return reject(new Error('canvas 不可用'))
        ctx.drawImage(img, 0, 0, w, h)
        canvas.toBlob(
          (blob) => (blob ? resolve(blob) : reject(new Error('压缩失败'))),
          'image/jpeg',
          quality,
        )
      }
      img.src = reader.result as string
    }
    reader.readAsDataURL(file)
  })
}
