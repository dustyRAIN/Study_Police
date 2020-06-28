from django.contrib import admin
from .models import Materials



class UserAdmin(admin.ModelAdmin):
    pass

admin.site.register(Materials, UserAdmin)
