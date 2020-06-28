from django.db import models
from classes.models import Classroom

class Materials(models.Model):
    id = models.AutoField(primary_key = True)
    classroom = models.ForeignKey(Classroom, on_delete=models.CASCADE)
    name = models.CharField(max_length = 200)
    the_file = models.FileField(upload_to = 'Materials/')
    date_uploaded = models.DateField(auto_now_add=True)

    def getMatId(self):
        return self.id

    def __str__(self):
        return str(self.id) + " " + str(self.classroom) + " " +str(self.name)

    class Meta:
        ordering = ['classroom', 'date_uploaded', 'name']

    def save(self, force_insert=False, force_update=False, using=None, update_fields=None):
        self.the_file.name = str(self.name) + '.pdf'
        return super().save(force_insert=force_insert, force_update=force_update, using=using, update_fields=update_fields)