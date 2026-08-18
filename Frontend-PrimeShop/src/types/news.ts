export interface News {
  id: number;
  excerpt: string;
  imageUrl: string;
  publishedAt: string;
  slug: string;
  status: 'PUBLISHED' | 'DRAFT';
  title: string;
  viewCount: number;
  authorId: number | null;
  categoryId: number;
  textUrl: string;
  }