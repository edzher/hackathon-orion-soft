// src/context/FiltersContext.tsx
'use client'

import React, { createContext, useContext, useState } from 'react'

export type FiltersData = {
    job: string
    experience?: number
    city?: string
    minSalary?: number
    maxSalary?: number
    startDate?: string
    endDate?: string
    telegram: boolean
    headhunter: boolean
    superjob: boolean
    timeRange?: string
    page: number
    size: number
}

const formatDateToUTCMoscow = (date?: Date): string | undefined => {
    if (!date) return new Date().toISOString().split('T')[0];
    return date.toISOString().split('T')[0];
};


const defaultFilters: FiltersData = {
    job: 'Курьер',
    startDate: formatDateToUTCMoscow(undefined),
    endDate: formatDateToUTCMoscow(undefined),
    telegram: false,
    superjob: false,
    headhunter: false,
    page: 0,
    size: 20,
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
