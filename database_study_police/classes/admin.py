from django.contrib import admin

from .models import Classroom, ClassHasStudent



class UserAdmin(admin.ModelAdmin):
    pass

admin.site.register(Classroom, UserAdmin)
admin.site.register(ClassHasStudent, UserAdmin)
