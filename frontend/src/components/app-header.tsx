'use client'

import { Button } from "@/components/ui/button"
import { Separator } from "@/components/ui/separator"
import { SidebarTrigger } from "@/components/ui/sidebar"
import api from "@/lib/axios";
import { toast } from "sonner"

export function AppHeader() {

    const download = async () => {
        try {

            // TODO: Добавь загрузку файла
            const response = await api.post("/download", null, {
                responseType: "blob"
            })
            const blob = new Blob([response.data], { type: response.headers["content-type"] })
            const url = window.URL.createObjectURL(blob)

            const link = document.createElement("a")
            link.href = url
            link.download = "отчет по вакансиями.pdf"
            document.body.appendChild(link)
            link.click()

            window.URL.revokeObjectURL(url)
            document.body.removeChild(link)
        } catch (error) {
            console.log(error);
            toast.error("Ошибка при скачивании", {
                description: "Не удалось подключиться к серверу.",
            })
        }
    }

    return (
        <header className="flex h-(--header-height) shrink-0 items-center gap-2 border-b transition-[width,height] ease-linear group-has-data-[collapsible=icon]/sidebar-wrapper:h-(--header-height)">
            <div className="flex w-full items-center gap-1 px-4 lg:gap-2 lg:px-6">
                <SidebarTrigger className="-ml-1" />
                <Separator
                    orientation="vertical"
                    className="mx-2 data-[orientation=vertical]:h-4"
                />
                <h1 className="text-base font-medium">Анализатор вакансий</h1>
                <div className="ml-auto flex items-center gap-2">
                    <Button onClick={download}>
                        Скачать отчёт
                    </Button>
                </div>
            </div>
        </header>
    )
}
