from django.contrib import admin
from django.contrib.auth.decorators import login_required

from .models import ReadingTime, ContentFrequency

class UserAdmin(admin.ModelAdmin):
    pass

admin.site.register(ReadingTime, UserAdmin)
admin.site.register(ContentFrequency, UserAdmin)
