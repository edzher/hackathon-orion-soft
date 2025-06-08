// src/context/FiltersContext.tsx
'use client'

import React, { createContext, useContext, useState } from 'react'

export type FiltersData = {
    vacancy: string
    experience: number
    city: string
    minSalary: number
    maxSalary: number
    startDate?: string
    endDate?: string
    telegram: boolean
    headhunter: boolean
    superjob: boolean
    timeRange: string
    page: number
    limit: number
}

const defaultFilters: FiltersData = {
    vacancy: 'Курьер',
    experience: 0,
    startDate: '2023-04-03',
    endDate: '2025-04-03',
    city: 'Москва',
    minSalary: 0,
    maxSalary: 0,
    telegram: false,
    headhunter: true,
    superjob: false,
    timeRange: '3m',
    page: 0,
    limit: 10,
}

type FiltersContextType = {
    filters: FiltersData
    setFilters: React.Dispatch<React.SetStateAction<FiltersData>>
}

const FiltersContext = createContext<FiltersContextType | undefined>(undefined)

export const FiltersProvider = ({ children }: { children: React.ReactNode }) => {
    const [filters, setFilters] = useState<FiltersData>(defaultFilters)

    return (
        <FiltersContext.Provider value={{ filters, setFilters }}>
            {children}
        </FiltersContext.Provider>
    )
}

export const useFilters = () => {
    const context = useContext(FiltersContext)
    if (!context) {
        throw new Error('useFilters must be used within a FiltersProvider')
    }
    return context
}
