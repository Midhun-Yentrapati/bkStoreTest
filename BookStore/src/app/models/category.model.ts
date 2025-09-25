export interface CategoryModel {
    id: number; // Backend uses Long (number) type
    name: string;
    description?: string;
    slug?: string;
    isActive?: boolean;
    priority?: number;
    image?: string;
    createdAt?: string;
    updatedAt?: string;
}
