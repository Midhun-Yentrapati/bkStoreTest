export interface CategoryModel {
    id: string;
    name: string;
    slug?: string;
    description?: string;
    image?: string;
    displayOrder?: number;
    isActive?: boolean;
    createdAt?: string;
    updatedAt?: string;
    deletedAt?: string;
}
