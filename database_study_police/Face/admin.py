from django.contrib import admin
from .models import Face, TestFace


class UserAdmin(admin.ModelAdmin):
    pass

admin.site.register(Face, UserAdmin)
admin.site.register(TestFace, UserAdmin)