'use client'

import * as React from "react"
import { Area, AreaChart, CartesianGrid, XAxis } from "recharts"

import { useIsMobile } from "@/hooks/use-mobile"
import {
    Card,
    CardAction,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card"

import {
    ChartConfig,
    ChartContainer,
    ChartTooltip,
    ChartTooltipContent,
} from "@/components/ui/chart"

import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select"

import {
    ToggleGroup,
    ToggleGroupItem,
} from "@/components/ui/toggle-group"
import {useEffect, useState} from "react";
import {useFilters} from "@/context/filters";
import api from "@/lib/axios";
import {toast} from "sonner";
import {ProgressBar} from "@/components/progress-bar";
import {Alert, AlertDescription, AlertTitle} from "@/components/ui/alert";

const chartConfig = {
    desktop: {
        label: "Зарплата",
        color: "var(--primary)",
    },
} satisfies ChartConfig

export function ChartAreaInteractive() {
    const isMobile = useIsMobile()
    const [timeRange, setTimeRange] = useState("1m")
    const [chartSalary, setChartSalary] = useState<ChartSalary>()

    const {filters, setFilters} = useFilters()
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        if (isMobile) {
            setTimeRange("3m")
        }
    }, [isMobile])

    useEffect(() => {
        setFilters(prev => ({
            ...prev,
            timeRange: timeRange
        }))
    }, [timeRange]);

    useEffect(() => {
        try {
            api.get(`/api/reports/analytics/salary-by-day?from=${filters.startDate}&to=${filters.endDate}&job=${filters.vacancy}`)
                .then(res => {
                    console.log("res", res)
                    setChartSalary(res.data)
                })
                .catch(() => setError("Не удалось загрузить статистику"))
                .finally(() => setLoading(false))
            toast.info("Запрос данных для графика прошел успешно")
        } catch (error) {
            console.log(error);
            toast.error("Ошибка при отправке фильтров", {
                description: "Не удалось подключиться к серверу.",
            })
        }
    }, [filters.vacancy,
        filters.experience,
        filters.minSalary,
        filters.maxSalary,
        filters.city,
        filters.headhunter,
        filters.telegram,
        filters.superjob,
        filters.timeRange
    ]);

    if (loading) {
        return (
            <div className="p-6">
                <ProgressBar/>
            </div>
        )

    }

    if (error) {
        return (
            <div className="p-6">
                <Alert variant="destructive">
                    <AlertTitle>Ошибка</AlertTitle>
                    <AlertDescription>{error}</AlertDescription>
                </Alert>
            </div>
        )
    }

    return (
        <Card className="@container/card">
            <CardHeader>
                <CardTitle>Усреднённая зарплата по месяцам</CardTitle>
                <CardDescription>
                    <span className="@[540px]/card:hidden">Last 3 months</span>
                </CardDescription>
                <CardAction>
                    <ToggleGroup
                        type="single"
                        value={timeRange}
                        onValueChange={setTimeRange}
                        variant="outline"
                        className="hidden *:data-[slot=toggle-group-item]:!px-4 @[767px]/card:flex"
                    >
                        <ToggleGroupItem value="12m">12 месяцев</ToggleGroupItem>
                        <ToggleGroupItem value="9m">9 месяцев</ToggleGroupItem>
                        <ToggleGroupItem value="6m">6 месяцев</ToggleGroupItem>
                        <ToggleGroupItem value="3m">3 месяца</ToggleGroupItem>
                        <ToggleGroupItem value="1m">1 месяц</ToggleGroupItem>
                    </ToggleGroup>
                    <Select value={timeRange} onValueChange={setTimeRange}>
                        <SelectTrigger
                            className="flex w-40 **:data-[slot=select-value]:block **:data-[slot=select-value]:truncate @[767px]/card:hidden"
                            size="sm"
                            aria-label="Select a value"
                        >
                            <SelectValue placeholder="Last 3 months" />
                        </SelectTrigger>
                        <SelectContent className="rounded-xl">
                            <SelectItem value="12m" className="rounded-lg">
                                12 месяцев
                            </SelectItem>
                            <SelectItem value="9m" className="rounded-lg">
                                9 месяцев
                            </SelectItem>
                            <SelectItem value="6m" className="rounded-lg">
                                6 месяцев
                            </SelectItem>
                            <SelectItem value="3m" className="rounded-lg">
                                3 месяца
                            </SelectItem>
                            <SelectItem value="30d" className="rounded-lg">
                                1 месяц
                            </SelectItem>
                        </SelectContent>
                    </Select>
                </CardAction>
            </CardHeader>
            <CardContent className="px-2 pt-4 sm:px-6 sm:pt-6">
                <ChartContainer
                    config={chartConfig}
                    className="aspect-auto h-[250px] w-full"
                >
                    <AreaChart data={chartSalary}>
                        <defs>
                            <linearGradient id="fillDesktop" x1="0" y1="0" x2="0" y2="1">
                                <stop
                                    offset="5%"
                                    stopColor="var(--color-desktop)"
                                    stopOpacity={1.0}
                                />
                                <stop
                                    offset="95%"
                                    stopColor="var(--color-desktop)"
                                    stopOpacity={0.1}
                                />
                            </linearGradient>
                            <linearGradient id="fillMobile" x1="0" y1="0" x2="0" y2="1">
                                <stop
                                    offset="5%"
                                    stopColor="var(--color-mobile)"
                                    stopOpacity={0.8}
                                />
                                <stop
                                    offset="95%"
                                    stopColor="var(--color-mobile)"
                                    stopOpacity={0.1}
                                />
                            </linearGradient>
                        </defs>
                        <CartesianGrid vertical={false} />
                        <XAxis
                            dataKey="date"
                            tickLine={false}
                            axisLine={false}
                            tickMargin={8}
                            minTickGap={32}
                            tickFormatter={(value) => {
                                const date = new Date(value)
                                return date.toLocaleDateString("en-US", {
                                    month: "short",
                                    day: "numeric",
                                })
                            }}
                        />
                        <ChartTooltip
                            cursor={false}
                            content={
                                <ChartTooltipContent
                                    labelFormatter={(value) =>
                                        new Date(value).toLocaleDateString("en-US", {
                                            month: "short",
                                            day: "numeric",
                                        })
                                    }
                                    indicator="dot"
                                />
                            }
                        />
                        <Area
                            dataKey="desktop"
                            type="natural"
                            fill="url(#fillDesktop)"
                            stroke="var(--color-desktop)"
                            stackId="a"
                        />
                    </AreaChart>
                </ChartContainer>
            </CardContent>
        </Card>
    )
}
