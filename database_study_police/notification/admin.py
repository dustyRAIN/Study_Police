from django.contrib import admin
from django.contrib.auth.decorators import login_required

from .models import GeneralNotification

class UserAdmin(admin.ModelAdmin):
    pass

admin.site.register(GeneralNotification, UserAdmin)
