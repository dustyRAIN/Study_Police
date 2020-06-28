from django.contrib import admin
from django.contrib.auth.decorators import login_required

from .models import CustomUser, DemoPhoto, EmailByProvider





class UserAdmin(admin.ModelAdmin):
    pass

admin.site.register(CustomUser, UserAdmin)
admin.site.register(DemoPhoto, UserAdmin)
admin.site.register(EmailByProvider, UserAdmin)
"""admin.site.login = login_required(admin.site.login)"""
