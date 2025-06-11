'use client'

import {
    Card,
    CardDescription,
    CardFooter,
    CardHeader,
    CardTitle,
} from "@/components/ui/card"
import {useEffect, useState} from "react";
import api from "@/lib/axios";
import {toast} from "sonner";
import {useFilters} from "@/context/filters";
import {ProgressBar} from "@/components/progress-bar";
import {Alert, AlertDescription, AlertTitle} from "@/components/ui/alert";
import * as React from "react";

import salary_data from "./salary_data.json"

export function Statistics() {

    const [salaryStatistic, setSalaryStatistic] = useState<SalaryStatistic>()

    const {filters } = useFilters()
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        try {
            console.log(filters)
            api.post("/v1/dashboard/analytics/salary-statistic", filters, {
                headers: {
                    'Content-Type': 'application/json'
                }
            }).then(res => {
                setSalaryStatistic(salary_data)
            })
                .catch(() => setError("Не удалить загрузить данные"))
                .finally(() => setLoading(false))
            toast.info("Запрос данных для графика прошел успешно")
        } catch (error) {
            console.log(error);
            toast.error("Ошибка при отправке фильтров", {
                description: "Не удалось подключиться к серверу.",
            })
        }
    }, [
        filters.job,
        filters.experience,
        filters.minSalary,
        filters.maxSalary,
        filters.city,
        filters.headhunter,
        filters.telegram,
        filters.startDate,
        filters.endDate,
    ]);

    if (loading) {
        return (
            <div className="p-12">
                <ProgressBar/>
            </div>
        )

    }

    if (error) {
        return (
            <div className="p-12">
                <Alert variant="destructive">
                    <AlertTitle>Ошибка</AlertTitle>
                    <AlertDescription>{error}</AlertDescription>
                </Alert>
            </div>
        )
    }

    return (
        <div className="*:data-[slot=card]:from-primary/5 *:data-[slot=card]:to-card dark:*:data-[slot=card]:bg-card grid grid-cols-1 gap-4 px-4 *:data-[slot=card]:bg-gradient-to-t *:data-[slot=card]:shadow-xs lg:px-6 @xl/main:grid-cols-2 @5xl/main:grid-cols-3">
            <Card className="@container/card">
                <CardHeader>
                    <CardDescription>Медианная зарплата</CardDescription>
                    <CardTitle className="text-2xl font-semibold tabular-nums @[250px]/card:text-3xl">
                        {salaryStatistic?.median}₽
                    </CardTitle>
                </CardHeader>
                <CardFooter className="flex-col items-start gap-1.5 text-sm">
                    <div className="line-clamp-1 flex gap-2 font-medium">
                        {salaryStatistic?.start_date} - {salaryStatistic?.end_date}
                    </div>
                    <div className="text-muted-foreground">
                        Медианная зарплата за указанный период
                    </div>
                </CardFooter>
            </Card>
            <Card className="@container/card">
                <CardHeader>
                    <CardDescription>Минимальная зарплата</CardDescription>
                    <CardTitle className="text-2xl font-semibold tabular-nums @[250px]/card:text-3xl">
                        {salaryStatistic?.minimum}₽
                    </CardTitle>
                </CardHeader>
                <CardFooter className="flex-col items-start gap-1.5 text-sm">
                    <div className="line-clamp-1 flex gap-2 font-medium">
                        {salaryStatistic?.start_date} - {salaryStatistic?.end_date}
                    </div>
                    <div className="text-muted-foreground">
                        Минимальная зарплата за указанный период
                    </div>
                </CardFooter>
            </Card>
            <Card className="@container/card">
                <CardHeader>
                    <CardDescription>Максимальная зарплата</CardDescription>
                    <CardTitle className="text-2xl font-semibold tabular-nums @[250px]/card:text-3xl">
                        {salaryStatistic?.maximum}₽
                    </CardTitle>
                </CardHeader>
                <CardFooter className="flex-col items-start gap-1.5 text-sm">
                    <div className="line-clamp-1 flex gap-2 font-medium">
                        {salaryStatistic?.start_date} - {salaryStatistic?.end_date}
                    </div>
                    <div className="text-muted-foreground">
                        Максимальная зарплата за указанный период
                    </div>
                </CardFooter>
            </Card>
        </div>
    )
}
